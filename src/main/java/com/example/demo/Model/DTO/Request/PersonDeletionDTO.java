package com.example.demo.Model.DTO.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PersonDeletionDTO {
    @NotNull
    private Integer id;
    private String CSRFToken;
}
