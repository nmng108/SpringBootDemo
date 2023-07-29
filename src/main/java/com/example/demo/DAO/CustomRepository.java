package com.example.demo.DAO;

import com.example.demo.Model.DatabasePersonSearch;
import com.example.demo.Model.Entity.Person;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CustomRepository<T extends Object> {
    public List<Person> findByCriteria(DatabasePersonSearch criteria);
//    public
}
