package com.example.demo.Controller;

import com.example.demo.Model.DTO.Request.PersonCreationData;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.Entity.Person;
import com.example.demo.Service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

// may change Person to PersonDTO in responses

@RestController
@RequestMapping(path = "/api/persons")
public class PersonController {
    @Autowired
    private PersonService service;

    @GetMapping()
    public ResponseEntity<CommonResponse> getAll() {
        return ResponseEntity.ok(new CommonResponse(true, service.findAllPersons()));
    }

//    @GetMapping(name = "/{id}")
//    public ResponseEntity<CommonResponse> getPersonById(@PathVariable int id) {
//        return ResponseEntity.ok(new CommonResponse(true, service.findPersonById(id)));
//    }

//    @GetMapping(name = "/{identity}")
//    public ResponseEntity<CommonResponse> getPersonByIdentity(@PathVariable String identity) {
//        return ResponseEntity.ok(new CommonResponse(true, service.findPersonByIdentity(identity)));
//    }

    @PostMapping()
    public ResponseEntity<CommonResponse> create(@RequestBody PersonCreationData data) {
        try {
            Person newPerson = service.save(data);
            return newPerson != null ?
                    ResponseEntity
                            .created(new URI("/api/persons/" + String.valueOf(newPerson.getId())))
                            .body(CommonResponse.builder().success(true).build())
                    :
                    ResponseEntity
                            .internalServerError()
                            .body(CommonResponse.builder().success(false).errors("error while creating").build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new CommonResponse(false, "error while creating"));
        }
    }
}
