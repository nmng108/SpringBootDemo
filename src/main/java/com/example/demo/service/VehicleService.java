package com.example.demo.service;

import com.example.demo.model.dto.request.PersonUpdateDTO;
import com.example.demo.model.dto.request.VehicleCreationDTO;
import com.example.demo.model.dto.response.CommonResponse;
import org.springframework.http.ResponseEntity;

public interface VehicleService {
    ResponseEntity<CommonResponse> findAll();

    ResponseEntity<CommonResponse> findById(int id);

    ResponseEntity<CommonResponse> findByIdNumber(String identity);

    ResponseEntity<CommonResponse> save(VehicleCreationDTO data);

    ResponseEntity<CommonResponse> updateByIdNumber(int id, PersonUpdateDTO form);

    ResponseEntity<CommonResponse> delete(int id);
}
