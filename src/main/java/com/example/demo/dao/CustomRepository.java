package com.example.demo.dao;

import com.example.demo.model.EntitySearchModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface CustomRepository<T, ID, SearchModel extends EntitySearchModel<?>> extends JpaRepository<T, ID> {
    public List<T> findByCriteria(SearchModel criteria);

    public Long countByCriteria(SearchModel personSearch);
//    public
}
