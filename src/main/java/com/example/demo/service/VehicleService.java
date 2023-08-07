package com.example.demo.service;

import com.example.demo.dto.request.PersonUpdateDTO;
import com.example.demo.dto.request.VehicleCreationDTO;
import com.example.demo.dto.request.VehicleImageUploadDTO;
import com.example.demo.dto.response.CommonResponse;
import org.springframework.http.ResponseEntity;

public interface VehicleService {
    ResponseEntity<CommonResponse> findAll();

//    ResponseEntity<CommonResponse> findById(int id);

    ResponseEntity<CommonResponse> findByIdOrIdNumber(String idNumber);

    ResponseEntity<CommonResponse> save(VehicleCreationDTO data);

    ResponseEntity<CommonResponse> updateByIdNumber(int id, PersonUpdateDTO form);

    ResponseEntity<CommonResponse> delete(int id);

    ResponseEntity<CommonResponse> getImageAddresses(String idNumber);

    ResponseEntity<?> getImage(String idNumber, String imageId);

    ResponseEntity<CommonResponse> uploadImages(String idNumber, VehicleImageUploadDTO dto);
}
