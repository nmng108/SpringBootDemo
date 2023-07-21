package com.example.demo.Service.Impl;

import com.example.demo.DAO.PersonRepository;
import com.example.demo.Exception.HttpException;
import com.example.demo.Exception.InvalidRequestException;
import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.Entity.Person;
import com.example.demo.Service.PersonService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    @Getter
    private PersonRepository repository;

    @Override
    public ResponseEntity<CommonResponse> findAll() {
        return ResponseEntity.ok(new CommonResponse(true, (List<Person>) this.getRepository().findAll()));
    }

    @Override
    public ResponseEntity<CommonResponse> findById(int id) {
        Person person = this.getRepository().findById(id).orElse(null);

        if (person == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CommonResponse(true, person));
    }

    @Override
    public ResponseEntity<CommonResponse> findByIdAndIdentity(String identity) {
        Person person;

        try {
            person = this.getRepository().findById(Integer.parseInt(identity)).orElse(null);
            if (person == null) person = this.getRepository().findByIdentity(identity);
        } catch (NumberFormatException e) {
            person = this.getRepository().findByIdentity(identity);
        }

        if (person == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CommonResponse(true, person));
    }

    @Override
    public ResponseEntity<CommonResponse> save(PersonCreationDTO requestData) {
        // check if identity has existed
        ResponseEntity<CommonResponse> identityCheckResult = checkIfIdentityHasExisted(requestData.getIdentity());
        if (identityCheckResult != null) return identityCheckResult;

        Person newPerson = new Person(requestData);

        newPerson = this.getRepository().save(newPerson);

        try {
            return ResponseEntity
                    .created(new URI("/api/persons/" + newPerson.getId()))
                    .body(new CommonResponse(true, newPerson));
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI violated the specification");
        }
    }

    @Override
    public ResponseEntity<CommonResponse> updateById(int id, PersonUpdateDTO requestData) {
        Person modifiedPerson = this.getRepository().findById(id).orElse(null);

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

        modifiedPerson = this.getRepository().save(modifiedPerson);

        return ResponseEntity.ok(new CommonResponse(true, modifiedPerson));
    }

    @Override
    public ResponseEntity<CommonResponse> delete(int id) {
        this.getRepository().deleteById(id);
        return ResponseEntity.ok(new CommonResponse(true, "Person with id " + id + "has been deleted."));
    }

    private ResponseEntity<CommonResponse> checkIfIdentityHasExisted(String identity) {
        Person existingPerson = this.getRepository().findByIdentity(identity);

        if (existingPerson != null) {
            HashMap<String, String> error = new HashMap<>();
            error.put("identity", "\"identity\" has existed");
            HashMap<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("error_code", "E01");
            errorMessage.put("details", error);

            return ResponseEntity.badRequest().body(
                    new CommonResponse(false, errorMessage)
            );
        }

        return null;
    }
}
