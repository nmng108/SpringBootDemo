package com.example.demo.entity;

import com.example.demo.dto.request.VehicleCreationDTO;
import com.example.demo.dto.request.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Date;
import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "vehicles", indexes = {@Index(columnList = "identificationNumber")})
@EntityListeners(AuditingEntityListener.class)
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false, length = 25)
//    @NotBlank(message = "\"identificationNumber\" must not be empty")
    private String identificationNumber;

    @JoinColumn(name = "owner_id")
    @ManyToOne
    private Person owner;

    private VehicleType type;

    @Column(length = 15)
    private String brand;

    @Column(length = 15)
    private String model;
    //    @NotBlank(message = "birthDate must not be empty")
    private Date acquisitionDate;

    @Column(nullable = false)
    @CreatedDate
    private Instant createdAt;

    //    @Column(name = "modified_at")
    @Column(nullable = false)
    @LastModifiedDate
    private Instant modifiedAt;

    /**
     * Instantiate with minimum number of data
     */
    public Vehicle(String model, String identificationNumber) {
        this.model = model;
        this.identificationNumber = identificationNumber;
    }

    public Vehicle(@NotNull VehicleCreationDTO dto, Person owner) {
        this.identificationNumber = dto.getIdNumber();
        this.owner = owner;
        this.type = dto.getType();
        this.brand = dto.getBrand();
        this.model = dto.getModel();
        this.acquisitionDate = dto.getAcquisitionDate();
    }
}
