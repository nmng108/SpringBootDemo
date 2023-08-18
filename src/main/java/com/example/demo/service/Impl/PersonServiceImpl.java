package com.example.demo.service.Impl;

import com.example.demo.dao.PersonRepository;
import com.example.demo.dto.request.PersonCreationDTO;
import com.example.demo.dto.request.PersonSearchDTO;
import com.example.demo.dto.request.PersonUpdateDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.dto.response.PaginationSuccessResponse;
import com.example.demo.dto.response.PersonDTO;
import com.example.demo.entity.Person;
import com.example.demo.model.DatabasePersonSearch;
import com.example.demo.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;

    public PersonServiceImpl(PersonRepository personRepository) {
        this.personRepository = Objects.requireNonNull(personRepository);
    }

//    @Override
//    public ResponseEntity<CommonResponse> findAll(Sort sort) {
//        return ResponseEntity.ok(new CommonResponse(true, this.repository.findAll(sort)));
//    }

//    @Override
//    public ResponseEntity<CommonResponse> findById(int id) {
//        Person person = this.repository.findById(id).orElse(null);
//
//        if (person == null) return ResponseEntity.notFound().build();
//        return ResponseEntity.ok(new CommonResponse(true, person));
//    }

    @Override
    public ResponseEntity<CommonResponse> findByIdOrIdentity(String identity) {
        Person person = this.find(identity);

        if (person == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CommonResponse(true, new PersonDTO(person)));
    }

    @Override
    public ResponseEntity<?> findByCriteria(PersonSearchDTO dto) {
        DatabasePersonSearch personSearch = new DatabasePersonSearch(dto);
        Long counter = dto.getCount() != null
                ? (dto.getCount().equals(true) ? this.personRepository.countByCriteria(personSearch) : null)
                : null;
        List<Person> result = this.personRepository.findByCriteria(personSearch);

        return ResponseEntity.ok(dto.getPage() != null
                ? new PaginationSuccessResponse<>(true, result.stream().map(PersonDTO::new).toList(), counter, (long) dto.getSize())
                : new CommonResponse(true, result.stream().map(PersonDTO::new).toList())
        );
    }

    @Override
    public ResponseEntity<CommonResponse> save(PersonCreationDTO dto) {
        // check if identity has existed
        ResponseEntity<CommonResponse> identityCheckResult = this.checkIfIdentityHasExisted(dto.getIdentity());
        if (identityCheckResult != null) return identityCheckResult;

        Person newPerson = new Person(dto);

        newPerson = this.personRepository.save(newPerson);

        return ResponseEntity
                .created(URI.create("/api/persons/" + newPerson.getId()))
                .body(new CommonResponse(true, newPerson));
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

        modifiedPerson = this.personRepository.save(modifiedPerson);

        return ResponseEntity.ok(new CommonResponse(true, modifiedPerson));
    }

    @Override
    public ResponseEntity<CommonResponse> delete(int id) {
        this.personRepository.deleteById(id);
        return ResponseEntity.ok(new CommonResponse(true, "Person with id " + id + "has been deleted."));
    }

    private Person find(String identity) {
        Person person;

        try {
            person = this.personRepository.findById(Integer.parseInt(identity)).orElse(null);
            if (person == null) person = this.personRepository.findByIdentity(identity);
        } catch (NumberFormatException e) {
            person = this.personRepository.findByIdentity(identity);
        }

        return person;
    }

    private ResponseEntity<CommonResponse> checkIfIdentityHasExisted(String identity) {
        Person existingPerson = this.personRepository.findByIdentity(identity);

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
