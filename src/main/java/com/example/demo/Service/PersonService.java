package com.example.demo.Service;

import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonSearchDTO;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

public interface PersonService {
    ResponseEntity<CommonResponse> findAll(Sort sort);
    ResponseEntity<CommonResponse> findById(int id);
    ResponseEntity<CommonResponse> findByIdOrIdentity(String identity);
    ResponseEntity<CommonResponse> findByCriteria(PersonSearchDTO criteria);
    ResponseEntity<CommonResponse> save(PersonCreationDTO data);
    ResponseEntity<CommonResponse> updateByIdOrIdentity(String identity, PersonUpdateDTO form);
    ResponseEntity<CommonResponse> delete(int id);
}
