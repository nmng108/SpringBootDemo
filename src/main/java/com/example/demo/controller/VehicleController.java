package com.example.demo.controller;

import com.example.demo.dto.request.VehicleCreationDTO;
import com.example.demo.dto.request.VehicleDeletionDTO;
import com.example.demo.dto.request.VehicleImageUploadDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.service.VehicleImageService;
import com.example.demo.service.VehicleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping({"/api/vehicles", "/api/vehicles/"})
@Validated
public class VehicleController {
    private final VehicleService vehicleService;
    private final VehicleImageService vehicleImageService;

    public VehicleController(VehicleService vehicleService, VehicleImageService vehicleImageService) {
        this.vehicleService = Objects.requireNonNull(vehicleService);
        this.vehicleImageService = Objects.requireNonNull(vehicleImageService);
    }

    @GetMapping
    public ResponseEntity<CommonResponse> fetchAll() {
        return this.vehicleService.findAll();
    }

    @GetMapping({"/{identity}", "/{identity}/"})
    public ResponseEntity<CommonResponse> findOne(@PathVariable @Valid String identity) {
        return this.vehicleService.findByIdOrIdNumber(identity);
    }

    @PostMapping
    public ResponseEntity<CommonResponse> create(@RequestBody @Valid VehicleCreationDTO dto) {
        return this.vehicleService.save(dto);
    }

    @DeleteMapping
    public ResponseEntity<CommonResponse> delete(@RequestBody @Valid VehicleDeletionDTO dto) {
        return this.vehicleService.delete(dto.getId());
    }

    @GetMapping({"{id-number}/images", "{id-number}/images/"})
    public ResponseEntity<?> fetchVehicleImages(@PathVariable("id-number") @NotBlank String idNumber) {
        return this.vehicleImageService.getImageAddresses(idNumber);
    }

    @GetMapping({"{id-number}/images/{image-id}", "{id-number}/images/{image-id}/"})
    public ResponseEntity<?> fetchVehicleImage(@PathVariable("id-number") @NotBlank String idNumber,
                                               @PathVariable("image-id") @NotBlank String imageId) {
        return this.vehicleImageService.getImage(idNumber, imageId);
    }

    @PutMapping({"/{id-number}/images", "/{id-number}/images/"})
    public ResponseEntity<CommonResponse> uploadVehicleImage(@PathVariable("id-number") @NotBlank String idNumber,
                                                             @Valid VehicleImageUploadDTO dto) {
        return this.vehicleImageService.uploadImages(idNumber, dto);
    }

    @DeleteMapping({"/{id-number}/images", "/{id-number}/images/"})
    public ResponseEntity<CommonResponse> deleteImagesFromVehicle(@PathVariable("id-number") @NotBlank String idNumber) {
        return this.vehicleImageService.delete(idNumber);
    }
}
