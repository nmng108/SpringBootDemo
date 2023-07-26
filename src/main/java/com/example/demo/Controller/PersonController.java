package com.example.demo.Controller;

import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonDeletionDTO;
import com.example.demo.Model.DTO.Request.PersonSearchingDTO;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Service.PersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// may change Person to PersonDTO in responses

@RestController
@RequestMapping(path = "/api/persons")
@Validated
public class PersonController {
    @Autowired
    private PersonService service;

    @GetMapping()
    public ResponseEntity<CommonResponse> getPersons() {
        return this.service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse> getPersonById(
            @PathVariable @Pattern(regexp = "[0-9a-zA-Z]{1,15}", message = "Invalid resource id") String id
    ) {
        return this.service.findByIdOrIdentity(id);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<CommonResponse> search(PersonSearchingDTO criteria) {
        return this.service.findByCriteria(criteria);
    }

    @PostMapping()
    public ResponseEntity<CommonResponse> create(@RequestBody @Valid PersonCreationDTO data) {
        // TODO: prevent creating a person with identity that is similar to any route
        return this.service.save(data);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse> update(
            @PathVariable @Pattern(regexp = "[0-9a-zA-Z]{1,15}", message = "Invalid resource id") String id,
            @RequestBody @Valid PersonUpdateDTO requestData
    ) {
        return this.service.updateByIdOrIdentity(id, requestData);
    }

    @DeleteMapping()
    public ResponseEntity<CommonResponse> deleteById(@RequestBody @Valid PersonDeletionDTO form) {
        return this.service.delete(form.getId());
    }
}
