package com.example.demo.model.dto.response;

import com.example.demo.entity.Person;
import com.example.demo.entity.Vehicle;
import com.example.demo.model.dto.request.VehicleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.sql.Date;
import java.util.List;

@Data
public class PersonDTO {
    private Integer id;

    @NotBlank(message = "\"name\" must not be empty")
    @Length(min = 5, max = 45, message = "Invalid name length")
    private String name;

    private Date birthDate;

    @Digits(integer = 3, fraction = 2)
    @DecimalMin(value = "20")
    @DecimalMax(value = "300")
    private Double height; // unit: cm

    @Digits(integer = 3, fraction = 2)
    @DecimalMin(value = "20")
    @DecimalMax(value = "800")
    private Double weight;

    @Length(min = 5, max = 70, message = "Invalid address's character sequence length")
    private String address;

    @NotBlank(message = "\"identity\" must not be empty")
    @Length(min = 5, max = 15, message = "Invalid identity length")
    private String identity;

    List<VehicleWithPersonDTO> vehicles;

    public PersonDTO(Person person) {
        this.id = person.getId();
        this.name = person.getName();
        this.birthDate = person.getBirthDate();
        this.height = person.getHeight();
        this.weight = person.getWeight();
        this.address = person.getAddress();
        this.identity = person.getIdentity();
        this.vehicles = person.getVehicles().stream().map(VehicleWithPersonDTO::new).toList();
    }
}

@Data
class VehicleWithPersonDTO {
    private int id;
    private String identificationNumber;
    private VehicleType type;
    private Date acquisitionDate;

    public VehicleWithPersonDTO(Vehicle vehicle) {
        this.id = vehicle.getId();
        this.identificationNumber = vehicle.getIdentificationNumber();
        this.type = vehicle.getType();
        this.acquisitionDate = vehicle.getAcquisitionDate();
    }
}
