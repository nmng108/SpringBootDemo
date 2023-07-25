package com.example.demo.Controller;

import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonDeletionDTO;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Service.PersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// may change Person to PersonDTO in responses

@RestController
@RequestMapping(path = "/api/persons")
@Validated
public class PersonController {
    @Autowired
    @Getter
    private PersonService service;

    @GetMapping()
    public ResponseEntity<CommonResponse> getPersons() {
        return this.getService().findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse> getPersonById(
            @PathVariable @Pattern(regexp = "[0-9a-zA-Z]{1,15}", message = "Invalid resource id") String id
    ) {
        return this.getService().findByIdOrIdentity(id);
    }

    @PostMapping()
    public ResponseEntity<CommonResponse> create(@RequestBody @Valid PersonCreationDTO data) {
        return this.getService().save(data);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse> update(
            @PathVariable @Pattern(regexp = "[0-9a-zA-Z]{1,15}", message = "Invalid resource id") String id,
            @RequestBody @Valid PersonUpdateDTO requestData
    ) {
        return this.getService().updateByIdOrIdentity(id, requestData);
    }

    @DeleteMapping()
    public ResponseEntity<CommonResponse> deleteById(@RequestBody @Valid PersonDeletionDTO form) {
        return this.getService().delete(form.getId());
    }
}
