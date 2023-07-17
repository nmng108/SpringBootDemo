package com.example.demo.Service;

import com.example.demo.Model.DTO.Response.CommonResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IService {
    ResponseEntity<CommonResponse> findAll();
    ResponseEntity<CommonResponse> findById(int id);
    void delete(int id);
}
