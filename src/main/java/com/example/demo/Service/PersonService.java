package com.example.demo.Service;

import com.example.demo.Model.DTO.Request.PersonCreationData;
import com.example.demo.Model.DTO.Request.PersonUpdateForm;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.Entity.Person;
import org.springframework.http.ResponseEntity;

import java.net.URISyntaxException;
import java.util.List;

public interface PersonService extends IService {
    ResponseEntity<CommonResponse> findByIdentity(String identity);
    ResponseEntity<CommonResponse> save(PersonCreationData data) throws URISyntaxException;
    ResponseEntity<CommonResponse> updateById(int id, PersonUpdateForm form);
}
