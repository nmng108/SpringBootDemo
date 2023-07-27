package com.example.demo.DAO;

import com.example.demo.Model.Entity.Person;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

public interface CustomRepository<T extends Object> {
    public List<Person> findByCriteria(Set<String> criteria, boolean usingOr);
//    public
}
