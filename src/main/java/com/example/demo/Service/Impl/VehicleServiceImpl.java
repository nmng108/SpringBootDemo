package com.example.demo.Service.Impl;

import com.example.demo.DAO.PersonRepository;
import com.example.demo.DAO.VehicleRepository;
import com.example.demo.Model.DTO.Request.PersonUpdateDTO;
import com.example.demo.Model.DTO.Request.VehicleCreationDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.DTO.Response.VehicleDTO;
import com.example.demo.Model.Entity.Person;
import com.example.demo.Model.Entity.Vehicle;
import com.example.demo.Service.VehicleService;
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
        return ResponseEntity.ok(CommonResponse.builder().success(true).data(
                this.getVehicleRepository().findAll().stream().map(VehicleDTO::new).toList()
        ).build());
    }

    @Override
    public ResponseEntity<CommonResponse> findById(int id) {
        return ResponseEntity.ok(CommonResponse.builder().success(true).data(this.getVehicleRepository().findById(id)).build());
    }

    @Override
    public ResponseEntity<CommonResponse> findByIdNumber(String idNumber) {
        Vehicle vehicle = this.getVehicleRepository().findByIdentificationNumber(idNumber);
        if (vehicle == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(CommonResponse.builder().success(true).data(vehicle).build());
    }

    @Override
    public ResponseEntity<CommonResponse> save(VehicleCreationDTO data) throws URISyntaxException {
        Person person = this.getPersonRepository().findByIdentity(data.getOwnerIdentity());
        if (person == null) return ResponseEntity.status(404).body(
                CommonResponse.builder().success(false).errors("Person is not found").build()
        );

        Vehicle vehicle = this.getVehicleRepository().findByIdentificationNumber(data.getIdNumber());
        if (vehicle != null) {
            return ResponseEntity.badRequest().body(
                    CommonResponse.builder().success(false).errors(
                            "Vehicle with idNumber " + data.getIdNumber() + " has existed"
                    ).build()
            );
        }

        vehicle = this.getVehicleRepository().save(new Vehicle(data, person));

        return ResponseEntity.created(new URI("/api/vehicles/" + vehicle.getId())).body(
                CommonResponse.builder().success(true).data(new VehicleDTO(vehicle)).build()
        );
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
        return ResponseEntity.ok(new CommonResponse(true, "Vehicle with id " + id + " has been deleted."));
    }

    public ResponseEntity<CommonResponse> delete(String idNumber) {
        Vehicle vehicle = this.getVehicleRepository().findByIdentificationNumber(idNumber);
        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        this.getVehicleRepository().deleteById(vehicle.getId());
        return ResponseEntity.ok(new CommonResponse(true, "Vehicle with id " + idNumber + " has been deleted."));
    }
}
