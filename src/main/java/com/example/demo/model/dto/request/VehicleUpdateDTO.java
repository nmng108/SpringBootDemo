package com.example.demo.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.sql.Date;

@Data
public class VehicleUpdateDTO {
    @NotNull(message = "\"type\" must not be empty")
    private VehicleType type;

    @NotBlank(message = "\"idNumber\" must not be empty")
    @Length(min = 5, max = 25, message = "Invalid name length")
    private String idNumber;

    @NotBlank(message = "\"ownerIdentity\" must not be empty")
    @Length(min = 5, max = 15, message = "Invalid ownerIdentity length")
    private String ownerIdentity;

    @NotBlank(message = "\"branch\" must not be empty")
    @Length(min = 1, max = 15, message = "Invalid brand name length")
    private String brand;

    @Length(min = 5, max = 30, message = "Invalid model name length")
    private String model;

    @NotNull
    private Date acquisitionDate;
}
