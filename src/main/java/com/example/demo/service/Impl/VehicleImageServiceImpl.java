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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
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
    private final URI BASE_URI;

    private final int MAX_IMAGES;

    public VehicleImageServiceImpl(
            VehicleService vehicleService, VehicleImageRepository imageRepository,
            StorageProperties storageProperties, Environment environment
    ) throws IOException {

        this.vehicleService = Objects.requireNonNull(vehicleService);
        this.imageRepository = Objects.requireNonNull(imageRepository);
        this.storageProperties = Objects.requireNonNull(storageProperties);
        this.uploadLocation = Path.of(storageProperties.getVehicleImageLocation());

        if (!Files.exists(this.uploadLocation)) Files.createDirectories(this.uploadLocation);

        String HOSTNAME = Inet4Address.getLocalHost().getHostAddress();
        String PORT = environment.getProperty("server.port");
        String BASE_PATH = "/api/vehicles";
        this.BASE_URI = URI.create("http://%s:%s%s".formatted(HOSTNAME, PORT, BASE_PATH));

        this.MAX_IMAGES = storageProperties.getMaxImagesPerVehicle();
    }

    @Override
    public ResponseEntity<CommonResponse> getImageAddresses(String idNumber) {
        Vehicle vehicle = this.vehicleService.find(idNumber);

        if (vehicle == null) throw new ResourceNotFoundException();

        Set<VehicleImage> vehicleImages = this.imageRepository.findByVehicle(vehicle);

        if (vehicleImages.isEmpty()) throw new ResourceNotFoundException();

        List<String> result = vehicleImages.stream()
                .map(image -> "%s://%s:%s%s".formatted(
                        BASE_URI.getScheme(), BASE_URI.getHost(), BASE_URI.getPort(), image.getURI()))
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
        File[] files = this.uploadLocation.toAbsolutePath().toFile().listFiles();
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

    // this method should be made as a transaction
    @Override
    public ResponseEntity<CommonResponse> uploadImages(String vehicleIdNumber, VehicleImageUploadDTO dto) {
        this.validateImages(dto.getImages());

        Vehicle vehicle = this.vehicleService.find(vehicleIdNumber);

        if (vehicle == null) throw new ResourceNotFoundException();

        Set<VehicleImage> storedImages = this.imageRepository.findByVehicle(vehicle); // for counting purpose only

        if (storedImages.size() + dto.getImages().size() > MAX_IMAGES) {
            throw new InvalidRequestException(
                    "Remaining number of images can be uploaded is %d".formatted(MAX_IMAGES - storedImages.size())
            );
        }

        try {
            if (!Files.exists(this.uploadLocation)) Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new InternalServerException("Cannot create upload directory");
        }

        Set<String> result = new HashSet<>();

        for (MultipartFile image : dto.getImages()) {
            String[] contentType = Objects.requireNonNull(image.getContentType()).split("/");
            String fileType = contentType[1];
            String originalFilename = Objects.requireNonNull(image.getOriginalFilename()).strip();
            String newFilename;
            Path path;

            do {
                newFilename = UUID.randomUUID().toString().concat(".").concat(fileType);
                path = this.uploadLocation.resolve(newFilename).normalize().toAbsolutePath();
            } while (Files.exists(path));

            // TEST
            log.info(String.format("VehicleServiceImpl::uploadImages - original/new filenames: %s/%s",
                    originalFilename, newFilename));

            // store image
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new InternalServerException(e);
            }

            // insert new record containing the image information to database
            if (Files.exists(path)) {
                URI newImageUri = BASE_URI.resolve("/%s/images/%s".formatted(vehicleIdNumber, newFilename));
                VehicleImage vehicleImage = VehicleImage.builder()
                        .storedName(newFilename)
                        .originalName(originalFilename)
                        .URI(newImageUri.getPath())
                        .vehicle(vehicle)
                        .build();

                this.imageRepository.save(vehicleImage);
                result.add(newImageUri.toString());
            }
        }

        return ResponseEntity.created(BASE_URI.resolve("/" + vehicleIdNumber + "/images"))
                .body(new CommonResponse(true, result));
    }


    @Override
    public ResponseEntity<CommonResponse> updateByIdNumber(String vehicleIdNumber, Object dto) {
        // TODO: implement updateByIdNumber
        return null;
    }

    @Override
    public ResponseEntity<CommonResponse> delete(String vehicleIdNumber) {
        // TODO: implement delete
        return null;
    }

    @Override
    public ResponseEntity<CommonResponse> delete(String vehicleIdNumber, String imageId) {
        // TODO: implement delete
        return null;
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty() || images.size() > MAX_IMAGES) {
            throw new InvalidRequestException("Maximum number of images to be uploaded is %d"
                    .formatted(MAX_IMAGES));
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
                throw new InvalidRequestException("Invalid ContentType: %s".formatted(image.getContentType()));

            // check file type
            String[] contentType = image.getContentType().split("/");
            String[] filename = image.getOriginalFilename().split("/");

            boolean isNotAllowedType = !this.storageProperties.getAllowedImageTypes().contains(contentType[1]);

            if (isNotAllowedType || !contentType[1].equalsIgnoreCase(filename[filename.length - 1])) {
                throw new InvalidRequestException("ContentType \"%s\" is not allowed".formatted(image.getContentType()));
            }

            // check file size
            if (this.storageProperties.getMaxImageSizeInMB().toBytes() < image.getSize()) {
                throw new InvalidRequestException("File size of \"%s\" is larger than the allowed amount"
                        .formatted(image.getOriginalFilename()));
            }

            // safe-check if parent directory of file and the pre-selected directory are exactly the same
            Path parentPath = this.uploadLocation.resolve(image.getOriginalFilename())
                    .normalize().toAbsolutePath().getParent();

            if (!parentPath.equals(this.uploadLocation.toAbsolutePath())) {
                throw new InvalidRequestException(
                        "Cannot save the file \"%s\". This error may come from the filename."
                                .formatted(image.getOriginalFilename())
                );
            }
        }

    }
}
