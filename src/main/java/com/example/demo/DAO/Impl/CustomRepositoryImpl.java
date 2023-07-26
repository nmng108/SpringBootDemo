package com.example.demo.DAO.Impl;

import com.example.demo.DAO.CustomRepository;
import com.example.demo.Exception.InvalidRequestException;
import com.example.demo.Model.Entity.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.type.descriptor.java.CoercionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomRepositoryImpl<T extends Person> implements CustomRepository<T> {
    @PersistenceContext
    private EntityManager entityManager;

    // why needs to put Entity type/class into the places below??
    @Override
    public List<Person> findByCriteria(Set<String> criteria) {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = criteriaBuilder.createQuery(Person.class);
        Root<Person> person = query.from(Person.class);
        List<Predicate> predicates = new ArrayList<>();

        criteria.forEach(condition -> {
            String[] components = condition.split(" ");
            if (components.length < 3) {
                throw new RuntimeException("'" + condition + "'" + " does not follow the criteria format: " +
                        "'<field> <operator> <value>'");
            }

            String fieldName = components[0];
            String operator = components[1];
            String value = condition.split(components[1] + " ")[1]; // get the rest string
//            StringBuilder value = new StringBuilder();
//
//            for (int i = 2; i < components.length; i++) {
//                value.append(components[i]);
//                if (i < components.length - 1) value.append(" ");
//            }

            Predicate predicate;
            try {
                // org.springframework.dao.InvalidDataAccessApiUsageException: org.hibernate.query.SemanticException:
                //      Could not resolve attribute 'xxx' of 'com.example.demo.Model.Entity.Person'
                predicate = switch (operator) {
                    case "like" -> criteriaBuilder.like(person.get(fieldName), "%" + value + "%");
                    case "equal", "eq", "=" -> criteriaBuilder.equal(person.get(fieldName), value.toString());
                    // org.springframework.orm.jpa.JpaSystemException: Error coercing value
                    case "le", "<=" -> criteriaBuilder.le(person.get(fieldName), Double.parseDouble(value.toString()));
                    // org.springframework.dao.InvalidDataAccessApiUsageException: For input string: "4g9"
                    case "lt", "<" -> criteriaBuilder.lt(person.get(fieldName), Double.parseDouble(value.toString()));
                    case "ge", ">=" -> criteriaBuilder.ge(person.get(fieldName), Double.parseDouble(value.toString()));
                    case "gt", ">" -> criteriaBuilder.gt(person.get(fieldName), Double.parseDouble(value.toString()));
                    default -> throw new RuntimeException("operator is not in the allowed types");
                };
            } catch (CoercionException e) {
                throw new InvalidRequestException(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("Wrong field names");
            }

            predicates.add(predicate);
        });

        query.select(person)
                .where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }
}
