package com.example.demo.Controller;

import com.example.demo.Model.DTO.Request.PersonDeleteForm;
import com.example.demo.Model.DTO.Request.PersonCreationData;
import com.example.demo.Model.DTO.Request.PersonUpdateForm;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.Entity.Person;
import com.example.demo.Service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
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

    @GetMapping(path = "/{id}")
    public ResponseEntity<CommonResponse> getPersonById(@PathVariable int id) {
        Person person = service.findPersonById(id);
        if (person == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CommonResponse(true, person));
    }

//    duplicate with the route above; may put identity into body
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
            return ResponseEntity
                    .internalServerError()
                    .body(CommonResponse.builder().success(false).errors("error while creating").build());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse> update(@PathVariable int id, @RequestBody PersonUpdateForm form) {
        try {
            // some cases may happen and need to be returned by the service function:
            // 1. id is not found
            // 2. invalid data
            // 3. updating has not succeeded (by db connection, ...) (this err will be thrown as exception)
            boolean hasSucceeded = service.updateById(id, form);
            CommonResponse response = hasSucceeded ?
                    CommonResponse.builder().success(true).build()
                    :
                    CommonResponse.builder().success(false).errors("error...").build();
            return ResponseEntity.status(hasSucceeded ? 200 : 400).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body(CommonResponse.builder().success(false).errors("error...").build());
        }
    }

    @DeleteMapping()
    public ResponseEntity<CommonResponse> deleteById(@RequestBody PersonDeleteForm form) {
        service.delete(form.getId());
        return ResponseEntity.ok(new CommonResponse(true, "Person with id " + form.getId() + "has been deleted."));
    }
}
