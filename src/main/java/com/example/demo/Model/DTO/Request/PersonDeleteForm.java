package com.example.demo.Model.DTO.Request;

import lombok.Data;

public @Data class PersonDeleteForm {
    private int id;
    private String CSRFToken;
}
