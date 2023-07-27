package com.example.demo.Service;

import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Request.VehicleCreationDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import org.springframework.http.ResponseEntity;

import java.net.URISyntaxException;

public interface VehicleService {
    ResponseEntity<CommonResponse> findAll();

    ResponseEntity<CommonResponse> findById(int id);

    ResponseEntity<CommonResponse> findByIdNumber(String identity);

    ResponseEntity<CommonResponse> save(VehicleCreationDTO data);

    ResponseEntity<CommonResponse> updateByIdNumber(int id, PersonUpdateDTO form);

    ResponseEntity<CommonResponse> delete(int id);
}
