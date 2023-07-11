package com.example.demo.Model.DTO.Request;

import lombok.Data;

public @Data class PersonCreationData {
    private String name;
    private String birthDate;
    private double height; // unit: cm
    private double weight;
    private String address;
    private String identity;
}
