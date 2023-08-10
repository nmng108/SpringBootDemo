package com.example.demo.dao;

import com.example.demo.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "PersonData", path = "persons")
public interface PersonRepository extends JpaRepository<Person, Integer>, CustomRepository<Person> {
    Person findByIdentity(String identity);
}
