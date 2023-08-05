package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PersonUpdateDTO {
    @Length(min = 5, max = 45, message = "Invalid name length")
    private String name;
    @Pattern(regexp = "^((19[7-9][0-9])|([2-9][0-9]{3}))-((0?[1-9])|(1[0-2]))-((0?[1-9])|([12][0-9])|(3[01]))$",
            message = "Input did not match the requirement")
    private String birthDate;
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
    @Length(min = 5, max = 15, message = "Invalid identity length")
    private String identity;
}
