package com.example.demo.DAO.Impl;

import com.example.demo.DAO.CustomRepository;
import com.example.demo.Exception.InvalidRequestException;
import com.example.demo.Model.Entity.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.hibernate.query.SemanticException;
import org.hibernate.type.descriptor.java.CoercionException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomRepositoryImpl<T extends Person> implements CustomRepository<T> {
    @PersistenceContext
    private EntityManager entityManager;


    /**
     * Accept query with no predicate. If using the 'and' operator (by default),
     * the result will be identical to 'findAll()'.
     * If we use 'or', the response will not contain any record, because a default predicate '1 != 1' will be added.
     * @param criteria <propName: String, condition: String>
     * @param usingOr  boolean
     * @param sort     Sort
     * @return
     */
    @Override
    public List<Person> findByCriteria(Map<String, String> criteria, boolean usingOr, Sort sort) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = builder.createQuery(Person.class);
        Root<Person> person = query.from(Person.class);
        List<Predicate> predicates = new ArrayList<>();

        criteria.forEach((propName, condition) -> {
            String[] components = condition.split(" ");
            if (components.length < 2) {
                throw new RuntimeException(
                        "'" + condition + "'"
                                + " does not follow the criteria value format: '<operator> <value>'"
                );
            }

            String operator = components[0];
            // get the rest string; wrong if the substring [1] exists in the remaining value (may not in this case but be careful)
            String value = Arrays.stream(condition.split(components[0] + " "))
                    .reduce("", (cummulator, element) -> cummulator += element + " ").strip();
            Predicate predicate;

            try {
                // org.springframework.dao.InvalidDataAccessApiUsageException: org.hibernate.query.SemanticException:
                // Could not resolve attribute 'xxx' of 'com.example.demo.Model.Entity.Person'
                predicate = switch (operator) {
                    case "like" -> builder.like(person.get(propName), "%" + value + "%");
                    case "equal", "eq", "=" -> builder.equal(person.get(propName), value);
                    // org.springframework.orm.jpa.JpaSystemException: Error coercing value
                    case "le", "<=" -> builder.le(person.get(propName), Double.parseDouble(value));
                    // org.springframework.dao.InvalidDataAccessApiUsageException: For input string: "4g9"
                    case "lt", "<" -> builder.lt(person.get(propName), Double.parseDouble(value));
                    case "ge", ">=" -> builder.ge(person.get(propName), Double.parseDouble(value));
                    case "gt", ">" -> builder.gt(person.get(propName), Double.parseDouble(value));
                    default -> throw new RuntimeException("operator is not in the allowed types");
                };
            } catch (NumberFormatException e) {
                throw new InvalidRequestException("Wrong input value");
            } catch (CoercionException e) {
                throw new InvalidRequestException("Invalid numeric input value");
            }

            predicates.add(predicate);
        });

        query.select(person).where(usingOr ?
                builder.or(predicates.toArray(new Predicate[0])) // ???
                : builder.and(predicates.toArray(new Predicate[0]))
        );

        try {
            if (sort.isSorted()) {
                List<Order> orders = sort.stream().map(sortOrder -> sortOrder.isDescending() ?
                        builder.desc(person.get(sortOrder.getProperty()))
                        :
                        builder.asc(person.get(sortOrder.getProperty()))
                ).toList();

                return entityManager.createQuery(query.orderBy(orders)).getResultList();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("caught IllegalArgumentException");
            throw new InvalidRequestException("Wrong field name");
        }

        return entityManager.createQuery(query).getResultList();
    }
}
