package com.example.demo.Service.Impl;

import com.example.demo.DAO.PersonRepository;
import com.example.demo.Model.DTO.Request.PersonCreationData;
import com.example.demo.Model.DTO.Request.PersonUpdateForm;
import com.example.demo.Model.Entity.Person;
import com.example.demo.Service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    private PersonRepository repository;

    @Override
    public List<Person> findAllPersons() {
        return (List<Person>) repository.findAll(); // any other ways?
    }

    @Override
    public Person findPersonById(int id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Person findPersonByIdentity(String identity) {
//        return repository.findPersonByIdentity(identity);
        return null;
    }

    @Override
    public Person save(PersonCreationData personData) {
        // TODO: validate data before updating
        // if the identity has existed then response bad request/duplicate

        Person newPerson = Person.builder()
                .name(personData.getName())
                .identity(personData.getIdentity()).build();
//        Person newPerson = new Person(personData.getName(), personData.getIdentity());
//        newPerson.setBirthDate(personData.getBirthDate());
//        newPerson.setHeight(personData.getHeight());
//        newPerson.setWeight(personData.getWeight());
//        newPerson.setAddress(personData.getAddress());

        return repository.save(newPerson);
    }

    @Override
    public boolean updateById(int id, PersonUpdateForm form) {
        if (!repository.existsById(id)) {
            return false;
        }

        // TODO: validate data before updating

        Person modifiedPerson = repository.findById(id).get();
        if (form.getName() != null) modifiedPerson.setName(form.getName());
        if (form.getBirthDate() != null) modifiedPerson.setBirthDate(form.getBirthDate());
        if (form.getHeight() != null) modifiedPerson.setHeight(form.getHeight());
        if (form.getWeight() != null) modifiedPerson.setWeight(form.getWeight());
        if (form.getAddress() != null) modifiedPerson.setAddress(form.getAddress());
        if (form.getIdentity() != null) modifiedPerson.setIdentity(form.getIdentity());

        repository.save(modifiedPerson);
        return true;
    }

    @Override
    public void delete(int id) {
        repository.deleteById(id);
    }
}
