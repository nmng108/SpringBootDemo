package com.example.demo.DAO;

import com.example.demo.Model.Entity.Person;
import com.example.demo.Model.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    Vehicle findByIdentificationNumber(String identificationNumber);

    @Query("SELECT v FROM Vehicle v WHERE v.owner = ?1")
    List<Vehicle> findByOwnerIdentity(Person person);
}
