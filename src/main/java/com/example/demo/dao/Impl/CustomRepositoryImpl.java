package com.example.demo.dao.Impl;

import com.example.demo.dao.CustomRepository;
import com.example.demo.dto.request.SearchDTO;
import com.example.demo.entity.Person;
import com.example.demo.model.EntitySearchModel;
import com.example.demo.model.PersonSearchModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.Assert;

import java.util.List;

public class CustomRepositoryImpl<T, ID, SearchModel extends EntitySearchModel<D>, D extends SearchDTO> extends SimpleJpaRepository<T, ID>
        implements CustomRepository<T, ID, SearchModel> {
    //    @PersistenceContext
    private final EntityManager entityManager;

    public CustomRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    public CustomRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }

    protected <S, E extends T> Root<E> getRoot(CriteriaQuery<S> query, Class<E> domainClass) {
        Assert.notNull(domainClass, "Domain class must not be null");
        Assert.notNull(query, "CriteriaQuery must not be null");

        return query.from(domainClass);
    }

    /**
     * Accept query with no predicate. If using the 'and' operator (by default),
     * the result will be identical to 'findAll()'.
     * If we use 'or', the response will not contain any record, because a default predicate '1 != 1' will be added.
     *
     * @param searchModel PersonSearchModel
     * @return List<Person>
     */
    @Override
    public List<T> findByCriteria(SearchModel searchModel) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.getDomainClass());
        Root<T> root = this.getRoot(query, this.getDomainClass());
        List<Predicate> predicates = searchModel.criteriaToPredicates(builder, root);

        query.select(root).where(searchModel.isUsingOr()
                ? builder.or(predicates.toArray(new Predicate[0])) // ???
                : builder.and(predicates.toArray(new Predicate[0])));

        List<Order> orders = searchModel.sortToCriteriaOrders(builder, root); // usually contains 1 element

        // search without pagination
        if (searchModel.getPagination() == null) {
            return entityManager.createQuery(query.orderBy(orders)).getResultList();
        }

        return entityManager.createQuery(query.orderBy(orders))
                .setFirstResult(searchModel.getPagination().firstRecordNumber())
                .setMaxResults(searchModel.getPagination().getNumberOfRecords())
                .getResultList();
    }

    public Long countByCriteria(SearchModel searchModel) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Person> personRoot = query.from(Person.class);
        List<Predicate> predicates = searchModel.criteriaToPredicates(builder, personRoot);

        query.select(builder.count(personRoot)).where(searchModel.isUsingOr()
                ? builder.or(predicates.toArray(new Predicate[0])) // ???
                : builder.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getSingleResult();
    }
}
