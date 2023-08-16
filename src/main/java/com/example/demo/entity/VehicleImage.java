package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Slf4j
@Entity
@Table(name = "vehicle_images")
@EntityListeners(AuditingEntityListener.class)
public class VehicleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 60)
    private String storedName;

    @Column(nullable = false, length = 160)
    private String originalName;

    @Column(nullable = true, length = 150)
    private String URI;

    @JoinColumn(name = "vehicle_id")
    @ManyToOne
    private Vehicle vehicle;

    @Column(nullable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    @PostPersist
    public void postPersist() {
        log.info("Saved the image: {} | {}", this.originalName, this.storedName);
    }

    @PostRemove
    public void postRemove() {
        log.info("Removed the image: {} | {}", this.originalName, this.storedName);
    }
}
