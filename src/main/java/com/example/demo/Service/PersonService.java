package com.example.demo.Service;

import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import org.springframework.http.ResponseEntity;

public interface PersonService extends IService {
    ResponseEntity<CommonResponse> findByIdOrIdentity(String identity);
    ResponseEntity<CommonResponse> save(PersonCreationDTO data);
    ResponseEntity<CommonResponse> updateByIdOrIdentity(String identity, PersonUpdateDTO form);
}
