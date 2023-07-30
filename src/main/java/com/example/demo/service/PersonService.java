package com.example.demo.service;

import com.example.demo.model.dto.request.PersonCreationDTO;
import com.example.demo.model.dto.request.PersonSearchDTO;
import com.example.demo.model.dto.request.PersonUpdateDTO;
import com.example.demo.model.dto.response.CommonResponse;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

public interface PersonService {
//    ResponseEntity<CommonResponse> findAll(Sort sort);
//    ResponseEntity<CommonResponse> findById(int id);
    ResponseEntity<CommonResponse> findByIdOrIdentity(String identity);
    ResponseEntity<?> findByCriteria(PersonSearchDTO criteria);
    ResponseEntity<CommonResponse> save(PersonCreationDTO data);
    ResponseEntity<CommonResponse> updateByIdOrIdentity(String identity, PersonUpdateDTO form);
    ResponseEntity<CommonResponse> delete(int id);
}
