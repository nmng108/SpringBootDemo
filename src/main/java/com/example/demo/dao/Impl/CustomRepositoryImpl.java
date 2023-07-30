package com.example.demo.dao.Impl;

import com.example.demo.dao.CustomRepository;
import com.example.demo.model.DatabasePersonSearch;
import com.example.demo.model.entity.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import java.util.List;

public class CustomRepositoryImpl<T extends Person> implements CustomRepository<T> {
    @PersistenceContext
    private EntityManager entityManager;

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

        // search without pagination
        if (personSearch.getPagination() == null) {
            return entityManager.createQuery(query.orderBy(orders)).getResultList();
        }

        return entityManager.createQuery(query.orderBy(orders))
                .setFirstResult(personSearch.getPagination().firstRecordNumber())
                .setMaxResults(personSearch.getPagination().getNumberOfRecords())
                .getResultList();
    }
}
