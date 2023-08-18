package com.example.demo.service.Impl;

import com.example.demo.configuration.StorageProperties;
import com.example.demo.dao.VehicleImageRepository;
import com.example.demo.dto.request.VehicleImageUploadDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.entity.Vehicle;
import com.example.demo.entity.VehicleImage;
import com.example.demo.exception.InternalServerException;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.VehicleImageService;
import com.example.demo.service.VehicleService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@Service
public class VehicleImageServiceImpl implements VehicleImageService {
    private final VehicleService vehicleService;
    private final VehicleImageRepository imageRepository;
    private final StorageProperties storageProperties;
    private final Path uploadLocation;
    private final String BASE_PATH = "/vehicles";
    private final String BASE_URI;
    private final int MAX_IMAGES;

    public VehicleImageServiceImpl(
            VehicleService vehicleService, VehicleImageRepository imageRepository,
            StorageProperties storageProperties, Environment environment, EntityManager entityManager
    ) throws IOException {

        this.vehicleService = Objects.requireNonNull(vehicleService);
        this.imageRepository = Objects.requireNonNull(imageRepository);
        this.storageProperties = Objects.requireNonNull(storageProperties);
        this.uploadLocation = Path.of(storageProperties.getVehicleImageLocation()).toAbsolutePath();

        if (!Files.exists(this.uploadLocation)) Files.createDirectories(this.uploadLocation);

        String HOSTNAME = environment.getProperty("server.hostname");
        String PORT = environment.getProperty("server.port");
        this.BASE_URI = URI.create("http://%s:%s/api/".formatted(HOSTNAME, PORT)).toString();

        this.MAX_IMAGES = storageProperties.getMaxImagesPerVehicle();
    }

    @Override
    public ResponseEntity<CommonResponse> getImageAddresses(String idNumber) {
        Vehicle vehicle = this.vehicleService.find(idNumber);

        if (vehicle == null) throw new ResourceNotFoundException();

        Set<VehicleImage> vehicleImages = this.imageRepository.findByVehicle(vehicle);

        if (vehicleImages.isEmpty()) throw new ResourceNotFoundException();

        List<String> result = vehicleImages.stream()
                .map(image -> BASE_URI.concat(image.getURI()))
                .toList();

        return ResponseEntity.ok(new CommonResponse(true, result));
    }

    @Override
    public ResponseEntity<byte[]> getImage(String vehicleIdNumber, String imageId) {
        Vehicle vehicle = this.vehicleService.find(vehicleIdNumber);

        if (vehicle == null) throw new ResourceNotFoundException();

        List<VehicleImage> vehicleImages = this.imageRepository.findByVehicle(vehicle).stream().toList();
        vehicleImages = vehicleImages.stream().filter(image -> {
            String[] splitName = image.getStoredName().split("\\.");

            if (splitName.length != 2) return false;

            String fileNameWithoutType = splitName[0];

            return fileNameWithoutType.equals(imageId) || image.getStoredName().equals(imageId);
        }).toList();

        // check if the image belongs to the specified vehicle exists => vehicleImages.size() must be 1
        if (vehicleImages.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        if (vehicleImages.size() > 1) {
            throw new RuntimeException("Exist %s files with the same name \"%s\"".formatted(
                    vehicleImages.size(), imageId
            ));
        }

        File file = null;
        File[] files = this.uploadLocation.toFile().listFiles();
        String fileType = "";

        // traverse the upload directory to search for the image
        if (files != null) {
            for (File tmpFile : files) {
                String[] splitName = tmpFile.getName().split("\\.");

                if (!tmpFile.isFile() || splitName.length != 2) continue;

                String fileNameWithoutType = splitName[0];

                if (fileNameWithoutType.equals(imageId) || tmpFile.getName().equals(imageId)) {
                    file = tmpFile;
                    fileType = splitName[1];
                    break;
                }
            }
        }

        try {
            if (file != null && !fileType.isBlank() && Files.isReadable(file.toPath())) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "image/%s".formatted(fileType))
                        .body(Files.readAllBytes(file.toPath()));
            } else if (file == null) {
                // delete record in db if file doesn't exist
                this.imageRepository.delete(vehicleImages.get(0));
            }
        } catch (IOException e) {
            throw new InternalServerException(e);
        }

        throw new ResourceNotFoundException();
    }

    // try adding/updating images through updating the persisted vehicle entity (utilize the persistence context mechanism)
    @Override
    @Transactional
    public ResponseEntity<CommonResponse> uploadImages(String vehicleIdNumber, VehicleImageUploadDTO dto) {
        this.validateImages(dto.getImages());

        Vehicle vehicle = this.vehicleService.find(vehicleIdNumber);

        if (vehicle == null) throw new ResourceNotFoundException();

        // for counting purpose only
        int storedImages = vehicle.getImages().size();
//        int storedImages = this.imageRepository.findByVehicle(vehicle).size();

        if (storedImages + dto.getImages().size() > MAX_IMAGES) {
            throw new InvalidRequestException(
                    "Remaining number of images can be uploaded is %d".formatted(MAX_IMAGES - storedImages)
            );
        }

        // try to create the upload directory again if it doesn't exist (which may be created in bootstrap)
        try {
            if (!Files.exists(this.uploadLocation)) Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new InternalServerException("Cannot create the upload directory");
        }

        String allImagesBasePath = URI.create(this.BASE_PATH.concat("/%s/images".formatted(vehicleIdNumber))).toString();
        Set<String> imagePaths = new HashSet<>();

        for (MultipartFile image : dto.getImages()) {
            String[] contentType = Objects.requireNonNull(image.getContentType()).split("/"); // checked before
            String fileType = contentType[1];
            String originalFilename = Objects.requireNonNull(image.getOriginalFilename()).strip(); // checked before
            String storedFilename;
            Path path;

            // generate an unique filename
            do {
                storedFilename = UUID.randomUUID().toString().concat(".").concat(fileType);
                path = this.uploadLocation.resolve(storedFilename).normalize();
            } while (Files.exists(path));

            // Test the custom transaction rollback
            if (fileType.matches("(jpg)|(jpeg)")) {
                log.error("uploadImages - Stopped saving at the image %s".formatted(originalFilename));
                throw new InvalidRequestException("jpg file is currently not allowed");
            }

            // register a file cleanup transaction listener, which will remove files if rollback transaction
            TransactionSynchronizationManager.registerSynchronization(new FileCleanupTransactionListener(path));

            // store image
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new InternalServerException(e);
            }

            // insert new record containing the image information to database
            if (Files.exists(path)) {
                // We will save uriPath "/vehicles/..." in db, which does not contain "http.../api" in the string.
                // This will help in case APIs are versioned like "/api/v1/vehicles/..."
                String specificPath = URI.create(allImagesBasePath.concat("/%s".formatted(storedFilename))).toString();
                String newImageUri = URI.create(this.BASE_URI.concat(specificPath)).toString();
                VehicleImage vehicleImage = VehicleImage.builder()
                        .storedName(storedFilename)
                        .originalName(originalFilename)
                        .URI(specificPath)
                        .vehicle(vehicle)
                        .build();

                this.imageRepository.save(vehicleImage);
                imagePaths.add(newImageUri);
            }
        }

        return ResponseEntity.created(URI.create(
                this.BASE_URI.concat(allImagesBasePath)
        )).body(new CommonResponse(true, imagePaths));
    }


    @Override
    public ResponseEntity<CommonResponse> updateByIdNumber(String vehicleIdNumber, Object dto) {
        // TODO: implement updateByIdNumber
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<CommonResponse> delete(String vehicleIdNumber) {
        // TODO: implement delete
        Vehicle vehicle = this.vehicleService.find(vehicleIdNumber);
        if (vehicle == null) return ResponseEntity.notFound().build();

        log.info("Vehicle images to be deleted: {}", vehicle);

        try {
            for (VehicleImage image : vehicle.getImages()) {
                Path imagePath = this.uploadLocation.resolve(image.getStoredName());
                this.imageRepository.delete(image);
                TransactionSynchronizationManager.registerSynchronization(new FileRestoreTransactionListener(imagePath));
                Files.deleteIfExists(imagePath);
                if (!Files.exists(imagePath)) log.info("Deleted {}", image.getStoredName());
            }
        } catch (IOException e) {
            throw new InternalServerException(e);
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CommonResponse> delete(String vehicleIdNumber, String imageId) {
        // TODO: implement delete
        return null;
    }

    // should return some file information like file type... instead of calculate them again
    private void validateImages(List<MultipartFile> images) {
        if (images == null) {
            throw new InvalidRequestException("No image presented");
        }

        if (images.isEmpty() || images.size() > MAX_IMAGES) {
            throw new InvalidRequestException("Number of images to be uploaded is in range from %d to %d"
                    .formatted(1, MAX_IMAGES));
        }

        for (MultipartFile image : images) {
            // check if file, filename or content type is null
            if (image.isEmpty()) {
                throw new InvalidRequestException("empty file");
            }

            if (image.getOriginalFilename() == null || image.getContentType() == null) {
                throw new InvalidRequestException("file name and ContentType cannot be null");
            }

            // check file name and content type header formats
            boolean invalidFilename = !image.getOriginalFilename().matches("[a-zA-Z0-9 +_\\-=$&#]{1,30}(.[A-Za-z0-9]{1,5})+");
            boolean invalidContentType = !image.getContentType().matches("image/[a-zA-Z0-9]{1,5}");

            if (invalidFilename)
                throw new InvalidRequestException("Invalid file name: %s".formatted(image.getOriginalFilename()));
            if (invalidContentType)
                throw new InvalidRequestException("Invalid Content-Type: %s".formatted(image.getContentType()));

            // check file type
            String[] contentType = image.getContentType().split("/");
            String[] filename = image.getOriginalFilename().split("\\.");
            String filenameExtension = filename[filename.length - 1];

            boolean isNotAllowedType = !this.storageProperties.getAllowedImageTypes().contains(contentType[1]);
            boolean matchJpegCase = contentType[1].matches("(jpg)|(jpeg)") && filenameExtension.matches("(jpg)|(jpeg)");
            boolean fileExtensionNotMatched = !contentType[1].equalsIgnoreCase(filenameExtension) && !matchJpegCase;

            if (isNotAllowedType || fileExtensionNotMatched) {
                throw new InvalidRequestException("Content-Type \"%s\" is not allowed".formatted(image.getContentType()));
            }

            // check file size
            // The file size is checked once by spring before approaching this method
            if (this.storageProperties.getMaxImageSizeInMB().toBytes() < image.getSize()) {
                throw new InvalidRequestException("File size of \"%s\" is larger than the allowed amount"
                        .formatted(image.getOriginalFilename()));
            }

            // safe-check if parent directory of the files and the pre-selected directory are exactly the same
            Path parentPath = this.uploadLocation.resolve(image.getOriginalFilename())
                    .normalize().toAbsolutePath().getParent();

            if (!parentPath.equals(this.uploadLocation)) {
                throw new InvalidRequestException(
                        "Cannot save the file \"%s\". This error may come from the filename."
                                .formatted(image.getOriginalFilename())
                );
            }
        }
    }
}

@Slf4j
class FileCleanupTransactionListener implements TransactionSynchronization {
    private final List<Path> paths;

    public FileCleanupTransactionListener(Path... paths) {
        this.paths = Arrays.asList(paths);
    }

    @Override
    public void afterCompletion(int status) {
        if (status == STATUS_ROLLED_BACK) {
            for (Path path : paths) {
                try {
                    if (!Files.isWritable(path)) {
                        log.warn("The file {} is not writable", path.toAbsolutePath());
                    }

                    if (!Files.deleteIfExists(path)) {
                        log.info("The file/directory \"{}\" does not exist.", path.toAbsolutePath());
                    }
                } catch (IOException e) {
                    log.warn("An I/O exception happened while deleting {}", path.toAbsolutePath());
                    throw new InternalServerException(e);
                }
            }

            log.info("Cleaned up uploaded files");
        }
    }
}

@Slf4j
class FileRestoreTransactionListener implements TransactionSynchronization {
    private final byte[] input;
    private final Path destination;

    public FileRestoreTransactionListener(byte[] input, Path destination) {
        this.input = input;
        this.destination = destination;
    }

    // use this constructor before deleting file
    public FileRestoreTransactionListener(Path path) throws IOException {
        this.input = Files.readAllBytes(path);
        this.destination = path;
    }

    @Override
    public void afterCompletion(int status) {
        if (status == STATUS_ROLLED_BACK) {
            try {
                if (!Files.exists(destination)) {
                    Files.copy(new ByteArrayInputStream(this.input), destination);
                }
            } catch (IOException e) {
                log.warn("An I/O exception happened while reverting {}", destination.toAbsolutePath());
                throw new InternalServerException(e);
            }

            log.info("Reverting file %s is done".formatted(this.destination));
        }
    }
}
