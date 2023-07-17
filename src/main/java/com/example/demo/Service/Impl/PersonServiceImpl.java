package com.example.demo.Service.Impl;

import com.example.demo.DAO.PersonRepository;
import com.example.demo.Model.DTO.Request.PersonCreationData;
import com.example.demo.Model.DTO.Request.PersonUpdateForm;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.Entity.Person;
import com.example.demo.Service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    private PersonRepository repository;

    @Override
    public ResponseEntity<CommonResponse> findAll() {
        return ResponseEntity.ok(new CommonResponse(true, (List<Person>) repository.findAll()));
    }

    @Override
    public ResponseEntity<CommonResponse> findById(int id) {
        Person person = repository.findById(id).orElse(null);

        if (person == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CommonResponse(true, person));
    }

    @Override
    public ResponseEntity<CommonResponse> findByIdentity(String identity) {
        Person person = repository.findByIdentity(identity);

        if (person == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(new CommonResponse(true, person));
    }

    @Override
    public ResponseEntity<CommonResponse> save(PersonCreationData requestData) throws URISyntaxException {
        // check if identity has existed
        ResponseEntity<CommonResponse> identityCheckResult = checkIfIdentityHasExisted(requestData.getIdentity());
        if (identityCheckResult != null) return identityCheckResult;

        Person newPerson = Person.builder()
                .name(requestData.getName())
                .birthDate(requestData.getBirthDate())
                .height(requestData.getHeight())
                .weight(requestData.getWeight())
                .address(requestData.getAddress())
                .identity(requestData.getIdentity())
                .build();

        newPerson = repository.save(newPerson);

        return ResponseEntity
                .created(new URI("/api/persons/" + newPerson.getId()))
                .body(CommonResponse.builder().success(true).build());
    }

    @Override
    public ResponseEntity<CommonResponse> updateById(int id, PersonUpdateForm requestData) {
        Person modifiedPerson = repository.findById(id).orElse(null);

        if (modifiedPerson == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity<CommonResponse> identityCheckResult = checkIfIdentityHasExisted(requestData.getIdentity());
        if (identityCheckResult != null) return identityCheckResult;

        // if not found
        if (requestData.getName() != null) modifiedPerson.setName(requestData.getName());
        if (requestData.getBirthDate() != null) modifiedPerson.setBirthDate(
                Date.valueOf(requestData.getBirthDate())
        );
//        if (requestData.getBirthDate() != null) System.out.println(modifiedPerson.getBirthDate());
        if (requestData.getHeight() != null) modifiedPerson.setHeight(requestData.getHeight());
        if (requestData.getWeight() != null) modifiedPerson.setWeight(requestData.getWeight());
        if (requestData.getAddress() != null) modifiedPerson.setAddress(requestData.getAddress());
        if (requestData.getIdentity() != null) modifiedPerson.setIdentity(requestData.getIdentity());

        modifiedPerson = repository.save(modifiedPerson);

        return ResponseEntity.ok(CommonResponse.builder().success(true).data(modifiedPerson).build());
    }

    @Override
    public void delete(int id) {
        repository.deleteById(id);
    }

    private ResponseEntity<CommonResponse> checkIfIdentityHasExisted(String identity) {
        Person existingPerson = repository.findByIdentity(identity);

        if (existingPerson != null) {
            HashMap<String, String> error = new HashMap<>();
            error.put("identity", "\"identity\" has existed");
            HashMap<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("error_code", "E01");
            errorMessage.put("details", error);

            return ResponseEntity.badRequest().body(
                    CommonResponse.builder().success(false).errors(errorMessage).build()
            );
        }

        return null;
    }
}
