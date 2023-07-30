package com.example.demo.dao;

import com.example.demo.model.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer>, CustomRepository<Person> {
    Person findByIdentity(String identity);
    // implement sorting, paging (pagination)
}
