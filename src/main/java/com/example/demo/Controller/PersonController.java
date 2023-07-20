package com.example.demo.Controller;

import com.example.demo.Model.DTO.Request.PersonDeletionDTO;
import com.example.demo.Model.DTO.Request.PersonCreationDTO;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.Entity.Person;
import com.example.demo.Service.PersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.net.URISyntaxException;
import java.util.HashMap;

// may change Person to PersonDTO in responses

@RestController
@RequestMapping(path = "/api/persons")
public class PersonController {
    @Autowired
    @Getter
    private PersonService service;

    @GetMapping()
    public ResponseEntity<CommonResponse> getPersons(@RequestParam(required = false) String identity) {
        if (identity != null) {
            if (identity.length() < 5 || identity.length() > 15) {
                return ResponseEntity.badRequest().body(
                        CommonResponse.builder().success(false).errors("Invalid identity").build()
                );
            }

            return this.getService().findByIdentity(identity);
        }

        return this.getService().findAll();
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<CommonResponse> getPersonById(@PathVariable String id) {
        return this.getService().findByIdentity(id);
    }

    @PostMapping()
    public ResponseEntity<CommonResponse> create(
            @RequestBody @Valid PersonCreationDTO data, BindingResult bindingResult
    ) {
        try {
            if (bindingResult.hasErrors()) return errorResponse(bindingResult);

            return this.getService().save(data);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("URI syntax violated the spec"); // ?
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse> update(@PathVariable int id,
                                                 @RequestBody @Valid PersonUpdateDTO requestData,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return errorResponse(bindingResult);

        return this.getService().updateById(id, requestData);
    }

    @DeleteMapping()
    public ResponseEntity<CommonResponse> deleteById(@RequestBody @Valid PersonDeletionDTO form) {
        return this.getService().delete(form.getId());
    }

    private ResponseEntity<CommonResponse> errorResponse(BindingResult bindingResult) {
        HashMap<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach((error) -> {
            errors.put(error.getField(), error.getDefaultMessage());
            System.out.println(error);
        });

        HashMap<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("error_code", "E00");
        errorMessage.put("details", errors);

        return ResponseEntity.badRequest().body(
                CommonResponse.builder().success(false).errors(errorMessage).build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleCommonExceptions(WebRequest request, Exception ex) {
        System.out.println(ex.getLocalizedMessage());

        return ResponseEntity.accepted().body(
                CommonResponse.builder().success(false).errors(ex.getLocalizedMessage()).build()
        );
    }
}
