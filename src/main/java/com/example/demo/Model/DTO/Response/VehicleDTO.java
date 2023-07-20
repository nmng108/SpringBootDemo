package com.example.demo.Model.DTO.Response;

import com.example.demo.Model.DTO.Request.VehicleType;
import com.example.demo.Model.Entity.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class VehicleDTO {
    @NotBlank(message = "\"idNumber\" must not be empty")
    @Length(min = 5, max = 25, message = "Invalid name length")
    private String idNumber;

//    @NotBlank(message = "\"name\" must not be empty")
//    @Length(min = 5, max = 45, message = "Invalid name length")
//    private String ownerName;

    private Map<String, Object> owner;
//    @NotBlank(message = "\"ownerIdentity\" must not be empty")
//    @Length(min = 5, max = 15, message = "Invalid ownerIdentity length")
//    private String ownerIdentity;

    @NotNull(message = "\"type\" must not be empty")
    private VehicleType type;

    @NotBlank(message = "\"branch\" must not be empty")
    @Length(min = 1, max = 15, message = "Invalid brand name length")
    private String brand;

    @Length(min = 5, max = 30, message = "Invalid model name length")
    private String model;

    @NotNull
    private Date acquisitionDate;

    public VehicleDTO(Vehicle vehicle) {
        this.idNumber = vehicle.getIdentificationNumber();
        this.type = vehicle.getType();
        this.brand = vehicle.getBrand();
        this.model = vehicle.getModel();
        this.acquisitionDate = vehicle.getAcquisitionDate();

        this.owner = new HashMap<>();
        this.owner.put("identity", vehicle.getOwner().getIdentity());
        this.owner.put("name", vehicle.getOwner().getName());
    }
}

