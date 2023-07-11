package com.example.demo.DAO;

import com.example.demo.Model.Entity.Person;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;

@Controller
public interface PersonRepository extends CrudRepository<Person, Integer> {
//    @Query("SELECT * FROM Person WHERE identity = ?1 ")
//    Person findPersonByIdentity(String identity);
}
