package com.example.demo.DAO.Impl;

import com.example.demo.DAO.CustomRepository;
import com.example.demo.Exception.InvalidRequestException;
import com.example.demo.Model.DatabasePersonSearch;
import com.example.demo.Model.Entity.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.hibernate.type.descriptor.java.CoercionException;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomRepositoryImpl<T extends Person> implements CustomRepository<T> {
    @PersistenceContext
    private EntityManager entityManager;

    private static int getNumberOfRecords(DatabasePersonSearch personSearch) {
        int startFrom = personSearch.getPagination().getFrom() * personSearch.getPagination().getSize();
        // should count before fetching (instead of using MAX_VALUE); and pass an argument to turn on/off it
        int numberOfRecords;

        if (personSearch.getPagination().getTo() == null) {
            numberOfRecords = startFrom + personSearch.getPagination().getSize();
        } else if (personSearch.getPagination().getTo() == Integer.MAX_VALUE) {
            numberOfRecords = personSearch.getPagination().getTo();
        } else {
            numberOfRecords = (personSearch.getPagination().getTo() - personSearch.getPagination().getFrom() + 1) * personSearch.getPagination().getSize();
        }

        return numberOfRecords;
    }

    /**
     * Accept query with no predicate. If using the 'and' operator (by default),
     * the result will be identical to 'findAll()'.
     * If we use 'or', the response will not contain any record, because a default predicate '1 != 1' will be added.
     *
     * @param personSearch DatabasePersonSearch
     * @return List<Person>
     */
    @Override
    public List<Person> findByCriteria(DatabasePersonSearch personSearch) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = builder.createQuery(Person.class);
        Root<Person> personRoot = query.from(Person.class);
        List<Predicate> predicates = personSearch.criteriaToPredicates(builder, personRoot);

        query.select(personRoot).where(personSearch.isUsingOr() ? builder.or(predicates.toArray(new Predicate[0])) // ???
                : builder.and(predicates.toArray(new Predicate[0])));

        List<Order> orders = personSearch.sortToCriteriaOrders(builder, personRoot); // usually contains 1 element

//        if (personSearch.getSort().isSorted()) {
//            return entityManager.createQuery(query.orderBy(orders)).getResultList();
//        }

        // search without pagination
        if (personSearch.getPagination() == null) {
            return entityManager.createQuery(query.orderBy(orders)).getResultList();
        }

        int numberOfRecords = getNumberOfRecords(personSearch);
        int firstResult = personSearch.getPagination().getFrom() * personSearch.getPagination().getSize();

        return entityManager.createQuery(query.orderBy(orders)).setFirstResult(firstResult).setMaxResults(numberOfRecords).getResultList();
    }
}
