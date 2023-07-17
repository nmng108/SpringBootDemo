package com.example.demo.DAO;

import com.example.demo.Model.Entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {
    Person findByIdentity(String identity);
}
