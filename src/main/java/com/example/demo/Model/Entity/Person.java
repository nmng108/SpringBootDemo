package com.example.demo.Model.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public @Data class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
//    @NotBlank(message = "\"name\" must not be empty")
    private String name;

    @Column(name = "birthdate")
//    @NotBlank(message = "birthDate must not be empty")
    private Date birthDate;
    private Double height; // unit: cm
    private Double weight;
    private String address;

    @Column(unique = true, nullable = false)
//    @NotBlank(message = "\"identity\" must not be empty")
    private String identity;

//    @Column(name = "created_at")
////    @Modi
//    private String createdAt;
//
//    @Column(name = "modifiedAt")
//    private String modifiedAt;

    /**
     * Instantiate with minimum number of data
     */
    public Person(String name, String identity) {
        this.name = name;
        this.identity = identity;
    }
}
