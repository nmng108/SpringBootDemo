package com.example.demo.entity;

import com.example.demo.model.dto.request.PersonCreationDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Person {
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
//    @Column(name = "modified_at")
//    private String modifiedAt;

    @OneToMany(mappedBy = "owner")
    private List<Vehicle> vehicles;

    /**
     * Instantiate with minimum number of data
     */
    public Person(String name, String identity) {
        this.name = name;
        this.identity = identity;
    }

    public Person(@NotNull PersonCreationDTO dto) {
        this.name = dto.getName();
        this.birthDate = dto.getBirthDate();
        this.height = dto.getHeight();
        this.weight = dto.getWeight();
        this.address = dto.getAddress();
        this.identity = dto.getIdentity();
    }
}
