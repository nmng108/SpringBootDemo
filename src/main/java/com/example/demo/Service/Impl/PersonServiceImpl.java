package com.example.demo.Service.Impl;

import com.example.demo.DAO.PersonRepository;
import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonSearchDTO;
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
    public ResponseEntity<CommonResponse> findByIdOrIdentity(String identity) {
        Person person = this.find(identity);

        if (person == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CommonResponse(true, person));
    }

    @Override
    public ResponseEntity<CommonResponse> findByCriteria(PersonSearchDTO criteria) {
        HashSet<String> stringCriteria = new HashSet<>();

        if (criteria.getId() != null) stringCriteria.add(criteria.getFormattedId());
        if (criteria.getIdentity() != null) stringCriteria.add(criteria.getFormattedIdentity());
        if (criteria.getName() != null) stringCriteria.add(criteria.getFormattedName());
        if (criteria.getAddress() != null) stringCriteria.add(criteria.getFormattedAddress());
        if (criteria.getBirthDate() != null) stringCriteria.add(criteria.getFormattedBirthDate());
        if (criteria.getHeight() != null) stringCriteria.add(criteria.getFormattedHeight());
        if (criteria.getWeight() != null) stringCriteria.add(criteria.getFormattedWeight());

        if (stringCriteria.isEmpty()) return this.findAll();

        List<Person> result = this.repository.findByCriteria(stringCriteria,
                criteria.getMode() != null && criteria.getMode().equals("or"));

        return result.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(new CommonResponse(true, result));
    }

    @Override
    public ResponseEntity<CommonResponse> save(PersonCreationDTO requestData) {
        // check if identity has existed
        ResponseEntity<CommonResponse> identityCheckResult = this.checkIfIdentityHasExisted(requestData.getIdentity());
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
    public ResponseEntity<CommonResponse> updateByIdOrIdentity(String identity, PersonUpdateDTO requestData) {

        Person modifiedPerson = this.find(identity);

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

    private Person find(String identity) {
        Person person;

        try {
            person = this.getRepository().findById(Integer.parseInt(identity)).orElse(null);
            if (person == null) person = this.getRepository().findByIdentity(identity);
        } catch (NumberFormatException e) {
            person = this.getRepository().findByIdentity(identity);
        }

        return person;
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
