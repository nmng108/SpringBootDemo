package com.example.demo.Model.DTO.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleDeletionDTO {
    @NotNull
    private Integer id;
    private String CSRFToken;
}
