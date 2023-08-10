package com.example.demo.dao;

import com.example.demo.entity.Person;
import com.example.demo.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "VehicleData", path = "vehicles")
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    Vehicle findByIdentificationNumber(String identificationNumber);

    @Query("SELECT v FROM Vehicle v WHERE v.owner = ?1")
    List<Vehicle> findByOwnerIdentity(Person person);
}
