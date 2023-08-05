package com.example.demo.service.Impl;

import com.example.demo.dao.PersonRepository;
import com.example.demo.dao.VehicleRepository;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.dto.request.PersonUpdateDTO;
import com.example.demo.dto.request.VehicleCreationDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.dto.response.VehicleDTO;
import com.example.demo.entity.Person;
import com.example.demo.entity.Vehicle;
import com.example.demo.service.VehicleService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class VehicleServiceImpl implements VehicleService {
    @Autowired
    @Getter
    private VehicleRepository vehicleRepository;

    @Autowired
    @Getter
    private PersonRepository personRepository;

    @Override
    public ResponseEntity<CommonResponse> findAll() {
        return ResponseEntity.ok(new CommonResponse(true,
                this.getVehicleRepository().findAll().stream().map(VehicleDTO::new).toList()
        ));
    }

    @Override
    public ResponseEntity<CommonResponse> findById(int id) {
        return ResponseEntity.ok(new CommonResponse(true, this.getVehicleRepository().findById(id)));
    }

    @Override
    public ResponseEntity<CommonResponse> findByIdNumber(String idNumber) {
        Vehicle vehicle = this.getVehicleRepository().findByIdentificationNumber(idNumber);
        if (vehicle == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(new CommonResponse(true, vehicle));
    }

    @Override
    public ResponseEntity<CommonResponse> save(VehicleCreationDTO data) {
        Person person = this.getPersonRepository().findByIdentity(data.getOwnerIdentity());
        if (person == null) return ResponseEntity.notFound().build();

        Vehicle vehicle = this.getVehicleRepository().findByIdentificationNumber(data.getIdNumber());
        try {
        if (vehicle != null) {
            throw new InvalidRequestException("Vehicle with idNumber " + data.getIdNumber() + " has existed");
        }

        vehicle = this.getVehicleRepository().save(new Vehicle(data, person));
            return ResponseEntity
                    .created(new URI("/api/vehicles/" + vehicle.getId()))
                    .body(new CommonResponse(true, new VehicleDTO(vehicle)));
        } catch (URISyntaxException e) {
            System.out.println(e.getInput());
            throw new RuntimeException("URI violated the specification");
        }
    }

    @Override
    public ResponseEntity<CommonResponse> updateByIdNumber(int id, PersonUpdateDTO form) {
        return null;
    }

    @Override
    public ResponseEntity<CommonResponse> delete(int id) {
        if (this.getVehicleRepository().findById(id).orElse(null) == null) {
            return ResponseEntity.notFound().build();
        }

        this.getVehicleRepository().deleteById(id);
        return ResponseEntity.ok(
                new CommonResponse(true, "Vehicle with id " + id + " has been deleted.")
        );
    }

    public ResponseEntity<CommonResponse> delete(String idNumber) {
        Vehicle vehicle = this.getVehicleRepository().findByIdentificationNumber(idNumber);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        this.getVehicleRepository().deleteById(vehicle.getId());
        return ResponseEntity.ok(
                new CommonResponse(true, "Vehicle with id " + idNumber + " has been deleted.")
        );
    }
}
