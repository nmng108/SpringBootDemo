package com.example.demo.service;

import com.example.demo.dto.request.VehicleCreationDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.entity.Vehicle;
import org.springframework.http.ResponseEntity;

public interface VehicleService {
    ResponseEntity<CommonResponse> findAll();

//    ResponseEntity<CommonResponse> findById(int id);

    ResponseEntity<CommonResponse> findByIdOrIdNumber(String idNumber);

    ResponseEntity<CommonResponse> save(VehicleCreationDTO data);

    ResponseEntity<CommonResponse> updateByIdNumber(int id, Object dto);

    ResponseEntity<CommonResponse> delete(int id);

    Vehicle find(String idNumber);
}
