package com.example.demo.service.Impl;

import com.example.demo.configuration.StorageProperties;
import com.example.demo.dao.PersonRepository;
import com.example.demo.dao.VehicleImageRepository;
import com.example.demo.dao.VehicleRepository;
import com.example.demo.dto.request.PersonUpdateDTO;
import com.example.demo.dto.request.VehicleCreationDTO;
import com.example.demo.dto.request.VehicleImageUploadDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.dto.response.VehicleDTO;
import com.example.demo.entity.Person;
import com.example.demo.entity.Vehicle;
import com.example.demo.entity.VehicleImage;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl implements VehicleService {
    private static final Logger log = LoggerFactory.getLogger(VehicleServiceImpl.class);
    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository imageRepository;
    private final PersonRepository personRepository;
    private final StorageProperties storageProperties;
    private final Path uploadLocation;
    private final String BASE_URI = "/api/vehicles";

    public VehicleServiceImpl(VehicleRepository vehicleRepository, VehicleImageRepository imageRepository, PersonRepository personRepository, StorageProperties storageProperties) throws IOException {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository);
        this.imageRepository = Objects.requireNonNull(imageRepository);
        this.personRepository = Objects.requireNonNull(personRepository);
        this.storageProperties = storageProperties;
        this.uploadLocation = Path.of(storageProperties.getVehicleImageLocation());
        if (!Files.exists(this.uploadLocation)) Files.createDirectories(this.uploadLocation);
    }

    @Override
    public ResponseEntity<CommonResponse> findAll() {
        return ResponseEntity.ok(new CommonResponse(true,
                this.vehicleRepository.findAll().stream().map(VehicleDTO::new).toList()
        ));
    }

//    @Override
//    public ResponseEntity<CommonResponse> findById(int id) {
//        return ResponseEntity.ok(new CommonResponse(true, this.vehicleRepository.findById(id)));
//    }

    @Override
    public ResponseEntity<CommonResponse> findByIdOrIdNumber(String idNumber) {
        Vehicle vehicle = this.find(idNumber);

        if (vehicle == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CommonResponse(true, new VehicleDTO(vehicle)));
    }

    @Override
    public ResponseEntity<CommonResponse> save(VehicleCreationDTO data) {
        Person person = this.personRepository.findByIdentity(data.getOwnerIdentity());
        if (person == null) throw new ResourceNotFoundException();

        Vehicle vehicle = this.vehicleRepository.findByIdentificationNumber(data.getIdNumber());

        if (vehicle != null) {
            throw new InvalidRequestException("Vehicle with idNumber %s has existed".formatted(data.getIdNumber()));
        }

        vehicle = this.vehicleRepository.save(new Vehicle(data, person));
        return ResponseEntity
                .created(URI.create("/api/vehicles/" + vehicle.getId()))
                .body(new CommonResponse(true, new VehicleDTO(vehicle)));
    }

    @Override
    public ResponseEntity<CommonResponse> updateByIdNumber(int id, PersonUpdateDTO form) {
        return null;
    }

    @Override
    public ResponseEntity<CommonResponse> delete(int id) {
        if (this.vehicleRepository.findById(id).orElse(null) == null) {
            return ResponseEntity.notFound().build();
        }

        this.vehicleRepository.deleteById(id);
        return ResponseEntity.ok(
                new CommonResponse(true, "Vehicle with id " + id + " has been deleted.")
        );
    }

    public ResponseEntity<CommonResponse> delete(String idNumber) {
        Vehicle vehicle = this.vehicleRepository.findByIdentificationNumber(idNumber);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        this.vehicleRepository.deleteById(vehicle.getId());
        return ResponseEntity.ok(
                new CommonResponse(true, "Vehicle with id " + idNumber + " has been deleted.")
        );
    }

    @Override
    public ResponseEntity<CommonResponse> getImageAddresses(String idNumber) {
        Vehicle vehicle = this.find(idNumber);

        if (vehicle == null) return ResponseEntity.notFound().build();

        Set<VehicleImage> vehicleImages = this.imageRepository.findByVehicle(vehicle);

        if (vehicleImages.isEmpty()) return ResponseEntity.notFound().build();

        List<String> result = vehicleImages.stream().map(VehicleImage::getURI).toList();

        return ResponseEntity.ok(new CommonResponse(true, result));
    }

    @Override
    public ResponseEntity<byte[]> getImage(String idNumber, String imageId) {
        Vehicle vehicle = this.find(idNumber);

        if (vehicle == null) return ResponseEntity.notFound().build();

        List<VehicleImage> vehicleImages = this.imageRepository.findByVehicle(vehicle).stream().toList();
        vehicleImages = vehicleImages.stream().filter(image -> {
            String[] splitName = image.getStoredName().split("\\.");

            if (splitName.length != 2) return false;

            String fileNameWithoutType = splitName[0];

            return fileNameWithoutType.equals(imageId) || image.getStoredName().equals(imageId);
        }).toList();

        // check if the image belongs to the specified vehicle exists => vehicleImages.size() must be 1
        if (vehicleImages.isEmpty()) {
            return ResponseEntity.notFound().build();
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
            throw new RuntimeException(e);
        }

        return ResponseEntity.notFound().build();
    }

    // this method should be a transaction
    @Override
    public ResponseEntity<CommonResponse> uploadImages(String idNumber, VehicleImageUploadDTO dto) {
        if (dto.getImages() == null || dto.getImages().isEmpty() || dto.getImages().size() > 3) {
            return ResponseEntity.badRequest().body(
                    new CommonResponse(false, "Invalid number of images")
            );
        }

        Vehicle vehicle = this.find(idNumber);
        Set<String> result = new HashSet<>();

        if (vehicle == null) return ResponseEntity.notFound().build();

        try {
            if (!Files.exists(this.uploadLocation)) Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory");
        }

        for (MultipartFile image : dto.getImages()) {
            // check if file, filename and content type exist
            if (image.isEmpty() || image.getOriginalFilename() == null || image.getContentType() == null) {
                return ResponseEntity.badRequest().body(
                        new CommonResponse(false, "empty file or invalid information")
                );
            }

            // check file size
            String[] contentType = image.getContentType().split("/");
            boolean isNotAllowedType = !("image".equals(contentType[0])
                    && this.storageProperties.getAllowedImageTypes().contains(contentType[1]));

            if (contentType.length != 2 || isNotAllowedType) {
                return ResponseEntity.badRequest().body(
                        new CommonResponse(false, "content type is not allowed")
                );
            }

            // check file type
            if (this.storageProperties.getMaxImageSizeInMB().toBytes() < image.getSize()) {
                return ResponseEntity.badRequest().body(
                        new CommonResponse(false, "File size is larger than the allowed amount")
                );
            }

            String fileType = contentType[1];
            String originalFilename = image.getOriginalFilename();
            String newFilename;
            Path path;

            do {
                newFilename = UUID.randomUUID().toString().concat("." + fileType);
                path = this.uploadLocation.resolve(newFilename).normalize().toAbsolutePath();
            } while (Files.exists(path));

            // check if parent directory of file and the selected one are exactly the same
            if (!path.getParent().equals(this.uploadLocation.toAbsolutePath())) {
                throw new InvalidRequestException(
                        String.format("Cannot save the file \"%s\". This error may come from the filename.", originalFilename)
                );
            }

            // TEST
            log.info(String.format("VehicleServiceImpl::uploadImages - old/new filenames: %s/%s",
                    originalFilename, newFilename));

            // store file
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // insert new record in database
            if (Files.exists(path)) {
                String URI = BASE_URI.concat(String.format("/%s/images/%s", idNumber, newFilename));
                VehicleImage vehicleImage = VehicleImage.builder()
                        .storedName(newFilename)
                        .originalName(originalFilename)
                        .URI(URI)
                        .vehicle(vehicle)
                        .build();

                this.imageRepository.save(vehicleImage);
                result.add(URI);
            }
        }

        return ResponseEntity.created(URI.create(BASE_URI.concat("/" + idNumber + "/images")))
                .body(new CommonResponse(true, result));
    }

    Vehicle find(String idNumber) {
        Vehicle vehicle;

        try {
            int id = Integer.parseInt(idNumber);
            vehicle = this.vehicleRepository.findById(id).orElse(null);

            if (Objects.isNull(vehicle)) {
                vehicle = this.vehicleRepository.findByIdentificationNumber(idNumber);
            }
        } catch (NumberFormatException e) {
            vehicle = this.vehicleRepository.findByIdentificationNumber(idNumber);
        }

        return vehicle;
    }
}
