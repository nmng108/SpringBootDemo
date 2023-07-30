package com.example.demo.Service.Impl;

import com.example.demo.DAO.PersonRepository;
import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonSearchDTO;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.DatabasePersonSearch;
import com.example.demo.Model.Entity.Person;
import com.example.demo.Service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<CommonResponse> findAll(Sort sort) {
        return ResponseEntity.ok(new CommonResponse(true, this.repository.findAll(sort)));
    }

    @Override
    public ResponseEntity<CommonResponse> findById(int id) {
        Person person = this.repository.findById(id).orElse(null);

        if (person == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CommonResponse(true, person));
    }

    @Override
    public ResponseEntity<CommonResponse> findByIdOrIdentity(String identity) {
        Person person = this.find(identity);

        if (person == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CommonResponse(true, person));
    }

    @Override
    public ResponseEntity<CommonResponse> findByCriteria(PersonSearchDTO dto) {
        DatabasePersonSearch personSearch = new DatabasePersonSearch(dto);
        List<Person> result = this.repository.findByCriteria(personSearch);

        return result.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(new CommonResponse(true, result));
    }

    @Override
    public ResponseEntity<CommonResponse> save(PersonCreationDTO dto) {
        // check if identity has existed
        ResponseEntity<CommonResponse> identityCheckResult = this.checkIfIdentityHasExisted(dto.getIdentity());
        if (identityCheckResult != null) return identityCheckResult;

        Person newPerson = new Person(dto);

        newPerson = this.repository.save(newPerson);

        try {
            return ResponseEntity
                    .created(new URI("/api/persons/" + newPerson.getId()))
                    .body(new CommonResponse(true, newPerson));
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI violated the specification");
        }
    }

    @Override
    public ResponseEntity<CommonResponse> updateByIdOrIdentity(String identity, PersonUpdateDTO dto) {

        Person modifiedPerson = this.find(identity);

        if (modifiedPerson == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity<CommonResponse> identityCheckResult = checkIfIdentityHasExisted(dto.getIdentity());
        if (identityCheckResult != null) return identityCheckResult;

        // if not found
        if (dto.getName() != null) modifiedPerson.setName(dto.getName());
        if (dto.getBirthDate() != null) modifiedPerson.setBirthDate(
                Date.valueOf(dto.getBirthDate())
        );
//        if (dto.getBirthDate() != null) System.out.println(modifiedPerson.getBirthDate());
        if (dto.getHeight() != null) modifiedPerson.setHeight(dto.getHeight());
        if (dto.getWeight() != null) modifiedPerson.setWeight(dto.getWeight());
        if (dto.getAddress() != null) modifiedPerson.setAddress(dto.getAddress());
        if (dto.getIdentity() != null) modifiedPerson.setIdentity(dto.getIdentity());

        modifiedPerson = this.repository.save(modifiedPerson);

        return ResponseEntity.ok(new CommonResponse(true, modifiedPerson));
    }

    @Override
    public ResponseEntity<CommonResponse> delete(int id) {
        this.repository.deleteById(id);
        return ResponseEntity.ok(new CommonResponse(true, "Person with id " + id + "has been deleted."));
    }

    private Person find(String identity) {
        Person person;

        try {
            person = this.repository.findById(Integer.parseInt(identity)).orElse(null);
            if (person == null) person = this.repository.findByIdentity(identity);
        } catch (NumberFormatException e) {
            person = this.repository.findByIdentity(identity);
        }

        return person;
    }

    private ResponseEntity<CommonResponse> checkIfIdentityHasExisted(String identity) {
        Person existingPerson = this.repository.findByIdentity(identity);

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
