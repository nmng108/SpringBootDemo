package com.example.demo.Controller;

import com.example.demo.Model.DTO.Request.VehicleCreationDTO;
import com.example.demo.Model.DTO.Request.VehicleDeletionDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
