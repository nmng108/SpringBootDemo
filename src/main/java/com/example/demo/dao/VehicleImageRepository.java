package com.example.demo.dao;

import com.example.demo.entity.Person;
import com.example.demo.entity.Vehicle;
import com.example.demo.entity.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface VehicleImageRepository extends JpaRepository<VehicleImage, Integer> {
    Set<VehicleImage> findByVehicle(Vehicle vehicle);
}
