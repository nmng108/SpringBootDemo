package com.example.demo.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.sql.Date;

@Data
public class PersonCreationDTO {
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
}
