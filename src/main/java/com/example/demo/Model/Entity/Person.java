package com.example.demo.Model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public @Data class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;

    @Column(name = "birthdate")
    private String birthDate;
    private double height; // unit: cm
    private double weight;
    private String address;

    @Column(nullable = false)
    private String identity;

    /**
     * Instantiate with minimum number of data
     * @param identity
     */
    public Person(String name, String identity) {
        this.identity = identity;
    }

//    public Person(int id, String name, String birthDate, double height, double weight, String address, String identity) {
//        this.id = id;
//        this.name = name;
//        this.birthDate = birthDate;
//        this.height = height;
//        this.weight = weight;
//        this.address = address;
//        this.identity = identity;
//    }

//    @Column(name = "created_at")
//    private String createdAt;
//
//    @Column(name = "modifiedAt")
//    private String modifiedAt;
}
