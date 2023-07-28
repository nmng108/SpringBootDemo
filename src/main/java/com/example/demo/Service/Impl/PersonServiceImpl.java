package com.example.demo.Service.Impl;

import com.example.demo.DAO.PersonRepository;
import com.example.demo.Exception.InvalidRequestException;
import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonSearchDTO;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.Entity.Person;
import com.example.demo.Service.PersonService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    private PersonRepository repository;

    @Override
    public ResponseEntity<CommonResponse> findAll(@NonNull Sort sort) {
            return ResponseEntity.ok(new CommonResponse(true, (List<Person>) this.repository.findAll(sort)));
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
    public ResponseEntity<CommonResponse> findByCriteria(PersonSearchDTO criteria) {
        // sortBy always is specified first. If done, order may be specified or not (asc by default)
        Sort sort = this.getSort(criteria.getSortBy(), criteria.getOrder());
        Map<String, String> formattedCriteria = criteria.getFormattedCriteria();

        if (formattedCriteria.isEmpty()) return this.findAll(sort);

        List<Person> result = this.repository.findByCriteria(formattedCriteria,
                criteria.getMode() != null && criteria.getMode().equals("or"), sort);

        return result.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(new CommonResponse(true, result));
    }

    private Sort getSort(String sortBy, String order) {
        if (order != null && sortBy == null) {
            throw new InvalidRequestException("\"sortBy\" must be specified along with \"order\"");
        }

        if (sortBy != null && Arrays.stream(PersonSearchDTO.class.getDeclaredFields())
                .noneMatch(field -> field.getName().equals(sortBy))) {
            throw new InvalidRequestException(sortBy + " is not a valid field");
        }

        Sort sort;

        if (order != null) {
            sort = switch (order) {
                case "asc" -> Sort.by(Sort.Direction.ASC, sortBy);
                case "desc" -> Sort.by(Sort.Direction.DESC, sortBy);
                default -> throw new RuntimeException("Invalid sorting order");
            };
        } else if (sortBy != null) {
            sort = Sort.by(Sort.Direction.ASC, sortBy);
        } else {
            sort = Sort.unsorted();
        }

        return sort;
    }

    @Override
    public ResponseEntity<CommonResponse> save(PersonCreationDTO requestData) {
        // check if identity has existed
        ResponseEntity<CommonResponse> identityCheckResult = this.checkIfIdentityHasExisted(requestData.getIdentity());
        if (identityCheckResult != null) return identityCheckResult;

        Person newPerson = new Person(requestData);

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
