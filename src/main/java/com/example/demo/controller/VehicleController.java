package com.example.demo.controller;

import com.example.demo.dto.request.VehicleCreationDTO;
import com.example.demo.dto.request.VehicleDeletionDTO;
import com.example.demo.dto.request.VehicleImageUploadDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping({"/api/vehicles", "/api/vehicles/"})
public class VehicleController {
    private final VehicleService service;

    public VehicleController(VehicleService vehicleService) {
        this.service = Objects.requireNonNull(vehicleService);
    }

    @GetMapping
    public ResponseEntity<CommonResponse> fetchAll() {
        return this.service.findAll();
    }

    @GetMapping({"/{identity}", "/{identity}/"})
    public ResponseEntity<CommonResponse> findOne(@PathVariable @Valid String identity) {
        return this.service.findByIdOrIdNumber(identity);
    }

    @PostMapping
    public ResponseEntity<CommonResponse> create(@RequestBody @Valid VehicleCreationDTO dto) {
        return this.service.save(dto);
    }

    @DeleteMapping
    public ResponseEntity<CommonResponse> delete(@RequestBody VehicleDeletionDTO dto) {
        return this.service.delete(dto.getId());
    }

    @GetMapping({"{id-number}/images", "{id-number}/images/"})
    public ResponseEntity<?> fetchVehicleImages(@PathVariable("id-number") String idNumber) {
        return this.service.getImageAddresses(idNumber);
    }

    @GetMapping({"{id-number}/images/{image-id}", "{id-number}/images/{image-id}/"})
    public ResponseEntity<?> fetchVehicleImage(@PathVariable("id-number") String idNumber, @PathVariable("image-id") String imageId) {
        return this.service.getImage(idNumber, imageId);
    }

    @PutMapping({"/{id-number}/images", "/{id-number}/images/"})
    public ResponseEntity<CommonResponse> uploadVehicleImage(@PathVariable("id-number") String idNumber, @Valid VehicleImageUploadDTO dto) {
        return this.service.uploadImages(idNumber, dto);
    }
}
