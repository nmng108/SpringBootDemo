package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PersonDeletionDTO {
    @NotNull
    private Integer id;
    private String CSRFToken;
}
