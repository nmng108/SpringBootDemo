package com.example.demo.dao;

import com.example.demo.entity.Person;
import com.example.demo.model.PersonSearchModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "PersonData", path = "persons")
public interface PersonRepository extends CustomRepository<Person, Integer, PersonSearchModel> {
    Person findByIdentity(String identity);
}
