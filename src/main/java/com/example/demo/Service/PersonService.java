package com.example.demo.Service;

import com.example.demo.Model.DTO.Request.PersonCreationData;
import com.example.demo.Model.DTO.Request.PersonUpdateForm;
import com.example.demo.Model.Entity.Person;

import java.util.List;

public interface PersonService {
    List<Person> findAllPersons();
    Person findPersonById(int id);
    Person findPersonByIdentity(String identity);
    Person save(PersonCreationData data);
    boolean updateById(int id, PersonUpdateForm form);
    void delete(int id);
}
