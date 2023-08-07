package com.example.demo.service.Impl;

import com.example.demo.configuration.StorageProperties;
import com.example.demo.dao.PersonRepository;
import com.example.demo.dao.VehicleRepository;
import com.example.demo.dto.request.PersonUpdateDTO;
import com.example.demo.dto.request.VehicleCreationDTO;
import com.example.demo.dto.request.VehicleImageUploadDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.dto.response.VehicleDTO;
import com.example.demo.entity.Person;
import com.example.demo.entity.Vehicle;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class VehicleServiceImpl implements VehicleService {
    private static final Logger log = LoggerFactory.getLogger(VehicleServiceImpl.class);
    private final VehicleRepository vehicleRepository;
    private final PersonRepository personRepository;
    private final StorageProperties storageProperties;
    private final Path uploadLocation;
    private final String BASE_URI = "/api/vehicles";

    public VehicleServiceImpl(VehicleRepository vehicleRepository, PersonRepository personRepository, StorageProperties storageProperties) throws IOException {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository);
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
        if (person == null) return ResponseEntity.notFound().build();

        Vehicle vehicle = this.vehicleRepository.findByIdentificationNumber(data.getIdNumber());

        if (vehicle != null) {
            throw new InvalidRequestException("Vehicle with idNumber " + data.getIdNumber() + " has existed");
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
    public ResponseEntity getImageAddresses(String idNumber) {
        Vehicle vehicle = this.find(idNumber);

        if (vehicle == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(idNumber);
    }

    @Override
    public ResponseEntity<byte[]> getImage(String idNumber, String imageId) {
        Vehicle vehicle = this.find(idNumber);

        // TODO: check if the image isn't belong to specified vehicle
        if (vehicle == null) return ResponseEntity.notFound().build();

        // TODO: retrieve image name (along with vehicle idNumber) in the db, then check if the image exists
        File file = null;// = this.uploadLocation.resolve(imageId).toAbsolutePath();
        File[] files = this.uploadLocation.toAbsolutePath().toFile().listFiles();

        if (files != null) {
            for (File tmpFile : files) {
                if (tmpFile.isFile() && tmpFile.getName().startsWith(imageId)) {
                    file = tmpFile;
                    break;
                }
            }
        }

        try {
            if (file != null && Files.isReadable(file.toPath())) {
                return ResponseEntity.ok(Files.readAllBytes(file.toPath()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<CommonResponse> uploadImages(String idNumber, VehicleImageUploadDTO dto) {
        if (dto.getImages() == null || dto.getImages().isEmpty() || dto.getImages().size() > 3) {
            return ResponseEntity.badRequest().body(
                    new CommonResponse(false, "Invalid number of images")
            );
        }

        Vehicle vehicle = this.find(idNumber);

        if (vehicle == null) return ResponseEntity.notFound().build();

        try {
            if (!Files.exists(this.uploadLocation)) Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> result = new ArrayList<>();

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

            log.info(String.format("VehicleServiceImpl::uploadImages - old/new filenames: %s/%s",
                    originalFilename, newFilename));

            try (InputStream inputStream = image.getInputStream()) {
                do {
                    Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
                } while (!Files.exists(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // insert new record in database

            result.add(this.BASE_URI.concat(String.format("/%s/images/%s", idNumber, newFilename)));
        }

        return ResponseEntity.created(URI.create(this.BASE_URI.concat("/" + idNumber + "/images")))
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
