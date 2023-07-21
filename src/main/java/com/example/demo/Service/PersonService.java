package com.example.demo.Service;

import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import org.springframework.http.ResponseEntity;

import java.net.URISyntaxException;

public interface PersonService extends IService {
    ResponseEntity<CommonResponse> findByIdAndIdentity(String identity);
    ResponseEntity<CommonResponse> save(PersonCreationDTO data);
    ResponseEntity<CommonResponse> updateById(int id, PersonUpdateDTO form);
}
