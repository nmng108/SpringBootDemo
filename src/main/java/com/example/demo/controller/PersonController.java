package com.example.demo.controller;

import com.example.demo.dto.request.PersonCreationDTO;
import com.example.demo.dto.request.PersonDeletionDTO;
import com.example.demo.dto.request.PersonSearchDTO;
import com.example.demo.dto.request.PersonUpdateDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.service.PersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// may change Person to PersonDTO in responses

@RestController
@RequestMapping({"/api/persons", "/api/persons/"})
@Validated
public class PersonController {
    @Autowired
    private PersonService service;

    @GetMapping
    public ResponseEntity<?> getPersons(@Valid PersonSearchDTO criteria) {
        // PersonSearchDTO object will always be created, even all its attributes are null.
        // After manually traversing through all attributes using 'if', service.findAll() will be
        // called if no attribute contains value.
        return this.service.findByCriteria(criteria);
    }

    @GetMapping({"/{id}", "/{id}/"})
    public ResponseEntity<CommonResponse> getPersonById(
            @PathVariable String id
    ) {
        return this.service.findByIdOrIdentity(id);
    }

    @PostMapping
    public ResponseEntity<CommonResponse> create(@RequestBody @Valid PersonCreationDTO data) {
        // TODO: prevent creating a person with identity that is similar to any route
        return this.service.save(data);
    }

    @PatchMapping({"/{id}", "/{id}/"})
    public ResponseEntity<CommonResponse> update(
            @PathVariable @Pattern(regexp = "[0-9a-zA-Z]{1,15}", message = "Invalid resource id") String id,
            @RequestBody @Valid PersonUpdateDTO requestData
    ) {
        return this.service.updateByIdOrIdentity(id, requestData);
    }

    @DeleteMapping
    public ResponseEntity<CommonResponse> deleteById(@RequestBody @Valid PersonDeletionDTO form) {
        return this.service.delete(form.getId());
    }
}
