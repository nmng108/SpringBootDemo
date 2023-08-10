package com.example.demo.service;

import com.example.demo.dto.request.VehicleImageUploadDTO;
import com.example.demo.dto.response.CommonResponse;
import org.springframework.http.ResponseEntity;

public interface VehicleImageService {
    ResponseEntity<CommonResponse> getImageAddresses(String idNumber);

    ResponseEntity<?> getImage(String vehicleIdNumber, String imageId);

    ResponseEntity<CommonResponse> uploadImages(String idNumber, VehicleImageUploadDTO dto);

    ResponseEntity<CommonResponse> updateByIdNumber(String vehicleIdNumber, Object dto);

    ResponseEntity<CommonResponse> delete(String vehicleIdNumber);

    ResponseEntity<CommonResponse> delete(String vehicleIdNumber, String imageId);
}
