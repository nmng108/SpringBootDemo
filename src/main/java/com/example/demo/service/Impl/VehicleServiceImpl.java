package com.example.demo.service.Impl;

import com.example.demo.dao.PersonRepository;
import com.example.demo.dao.VehicleRepository;
import com.example.demo.dto.request.VehicleCreationDTO;
import com.example.demo.dto.response.CommonResponse;
import com.example.demo.dto.response.VehicleDTO;
import com.example.demo.entity.Person;
import com.example.demo.entity.Vehicle;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

@Slf4j
@Service
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    private final PersonRepository personRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, PersonRepository personRepository) throws IOException {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository);
        this.personRepository = Objects.requireNonNull(personRepository);
    }

    @Override
    @Cacheable("vehicle-service")
    public ResponseEntity<CommonResponse> findAll() {
        try {
            Thread.sleep(4000L);
        } catch (Exception e) {
            log.debug(e.getLocalizedMessage());
        }

        return ResponseEntity.ok(new CommonResponse(true,
                this.vehicleRepository.findAll().stream().map(VehicleDTO::new).toList()
        ));
    }

//    @Override
//    public ResponseEntity<CommonResponse> findById(int id) {
//        return ResponseEntity.ok(new CommonResponse(true, this.vehicleRepository.findById(id)));
//    }

    @Override
    public ResponseEntity<CommonResponse> findByIdOrIdNumber(String idNumber) {
        Vehicle vehicle = this.find(idNumber);

        if (vehicle == null) throw new ResourceNotFoundException();

        return ResponseEntity.ok(new CommonResponse(true, new VehicleDTO(vehicle)));
    }

    @Override
    public ResponseEntity<CommonResponse> save(VehicleCreationDTO data) {
        Person person = this.personRepository.findByIdentity(data.getOwnerIdentity());
        if (person == null) throw new ResourceNotFoundException();

        Vehicle vehicle = this.vehicleRepository.findByIdentificationNumber(data.getIdNumber());

        if (vehicle != null) {
            throw new InvalidRequestException("Vehicle with idNumber %s has existed".formatted(data.getIdNumber()));
        }

        vehicle = this.vehicleRepository.save(new Vehicle(data, person));
        return ResponseEntity
                .created(URI.create("/api/vehicles/" + vehicle.getId()))
                .body(new CommonResponse(true, new VehicleDTO(vehicle)));
    }

    @Override
    public ResponseEntity<CommonResponse> updateByIdNumber(int id, Object dto) {
        // TODO: implement updateByIdNumber
        return null;
    }

    @Override
    public ResponseEntity<CommonResponse> delete(int id) {
        if (this.vehicleRepository.findById(id).orElse(null) == null) {
            throw new ResourceNotFoundException();
        }

        this.vehicleRepository.deleteById(id);
        return ResponseEntity.ok(
                new CommonResponse(true, "Vehicle with id " + id + " has been deleted.")
        );
    }

    public ResponseEntity<CommonResponse> delete(String idNumber) {
        Vehicle vehicle = this.vehicleRepository.findByIdentificationNumber(idNumber);
        if (vehicle == null) {
            throw new ResourceNotFoundException();
        }

        this.vehicleRepository.deleteById(vehicle.getId());
        return ResponseEntity.ok(
                new CommonResponse(true, "Vehicle with id " + idNumber + " has been deleted.")
        );
    }

    public Vehicle find(String idNumber) {
        Vehicle vehicle;

        try {
            int id = Integer.parseInt(idNumber);
            vehicle = this.vehicleRepository.findById(id).orElse(null);

            if (Objects.isNull(vehicle)) {
                vehicle = this.vehicleRepository.findByIdentificationNumber(idNumber);
            }
        } catch (NumberFormatException e) {
            vehicle = this.vehicleRepository.findByIdentificationNumber(idNumber);
        }

        return vehicle;
    }
}
