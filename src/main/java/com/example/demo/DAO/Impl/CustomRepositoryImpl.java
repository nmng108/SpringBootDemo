package com.example.demo.DAO.Impl;

import com.example.demo.DAO.CustomRepository;
import com.example.demo.Exception.InvalidRequestException;
import com.example.demo.Model.Entity.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.hibernate.type.descriptor.java.CoercionException;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomRepositoryImpl<T extends Person> implements CustomRepository<T> {
    @PersistenceContext
    private EntityManager entityManager;

    // accept query with no predicate. If using the 'and' operator (by default), the result will be identical
    // to 'findAll()'. If we use 'or', the response will not contain any record, because a default predicate '1 != 1'
    // will be added.
    @Override
    public List<Person> findByCriteria(Set<String> criteria, boolean usingOr, Sort sort) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = builder.createQuery(Person.class);
        Root<Person> person = query.from(Person.class);
        List<Predicate> predicates = new ArrayList<>();

        criteria.forEach(condition -> {
            String[] components = condition.split(" ");
            if (components.length < 3) {
                throw new RuntimeException(
                        "'" + condition + "'"
                                + " does not follow the criteria format: '<field> <operator> <value>'"
                );
            }

            String fieldName = components[0];
            String operator = components[1];
            // get the rest string; wrong if [0] = [1] (may not in this case but be careful)
            String value = condition.split(components[1] + " ")[1];
            Predicate predicate;

            try {
                // org.springframework.dao.InvalidDataAccessApiUsageException: org.hibernate.query.SemanticException:
                //      Could not resolve attribute 'xxx' of 'com.example.demo.Model.Entity.Person'
                predicate = switch (operator) {
                    case "like" -> builder.like(person.get(fieldName), "%" + value + "%");
                    case "equal", "eq", "=" -> builder.equal(person.get(fieldName), value);
                    // org.springframework.orm.jpa.JpaSystemException: Error coercing value
                    case "le", "<=" -> builder.le(person.get(fieldName), Double.parseDouble(value));
                    // org.springframework.dao.InvalidDataAccessApiUsageException: For input string: "4g9"
                    case "lt", "<" -> builder.lt(person.get(fieldName), Double.parseDouble(value));
                    case "ge", ">=" -> builder.ge(person.get(fieldName), Double.parseDouble(value));
                    case "gt", ">" -> builder.gt(person.get(fieldName), Double.parseDouble(value));
                    default -> throw new RuntimeException("operator is not in the allowed types");
                };
            } catch (CoercionException e) {
                throw new InvalidRequestException(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("Wrong field names");
            }

            predicates.add(predicate);
        });

        query.select(person).where(usingOr ?
                builder.or(predicates.toArray(new Predicate[0])) // ???
                : builder.and(predicates.toArray(new Predicate[0]))
        );

        if (sort.isSorted()) {
            List<Order> orders = sort.stream().map(sortOrder -> sortOrder.isDescending() ?
                    builder.desc(person.get(sortOrder.getProperty()))
                    :
                    builder.asc(person.get(sortOrder.getProperty()))
            ).toList();

            return entityManager.createQuery(query.orderBy(orders)).getResultList();
        }

        return entityManager.createQuery(query).getResultList();
    }
}
