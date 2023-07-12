package com.example.demo.Model.DTO.Request;

import lombok.Data;

public @Data class PersonUpdateForm {
//    private int id;
    private String name;
    private String birthDate;
    private Double height; // unit: cm
    private Double weight;
    private String address;
    private String identity;
}
