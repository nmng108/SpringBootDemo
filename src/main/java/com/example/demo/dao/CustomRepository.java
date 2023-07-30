package com.example.demo.dao;

import com.example.demo.model.DatabasePersonSearch;
import com.example.demo.entity.Person;

import java.util.List;

public interface CustomRepository<T extends Object> {
    public List<Person> findByCriteria(DatabasePersonSearch criteria);
//    public
}
