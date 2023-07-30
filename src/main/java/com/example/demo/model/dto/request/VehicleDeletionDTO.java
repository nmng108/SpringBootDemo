package com.example.demo.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleDeletionDTO {
    @NotNull
    private Integer id;
    private String CSRFToken;
}
