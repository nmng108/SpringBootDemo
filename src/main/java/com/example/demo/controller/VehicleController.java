package com.example.demo.controller;

import com.example.demo.model.dto.request.VehicleCreationDTO;
import com.example.demo.model.dto.request.VehicleDeletionDTO;
import com.example.demo.model.dto.response.CommonResponse;
import com.example.demo.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/vehicles", "/api/vehicles/"})
public class VehicleController {
    @Autowired
    private VehicleService service;

    @GetMapping
    public ResponseEntity<CommonResponse> fetchAll() {
        return this.service.findAll();
    }

    @PostMapping
    public ResponseEntity<CommonResponse> create(@RequestBody @Valid VehicleCreationDTO dto) {
        return this.service.save(dto);
    }

    @DeleteMapping
    public ResponseEntity<CommonResponse> delete(@RequestBody VehicleDeletionDTO dto) {
        return this.service.delete(dto.getId());
    }
}
