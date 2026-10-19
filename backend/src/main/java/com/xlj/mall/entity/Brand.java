package com.xlj.mall.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "brand")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String logo;

    @Column(name = "first_letter", nullable = false)
    private String firstLetter;

    private String description;

    @Column(nullable = false)
    private Integer sort;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        createdAt = now;
        updatedAt = now;
        if (sort == null) sort = 0;
        if (status == null) status = 1;
        if (firstLetter == null || firstLetter.isBlank()) firstLetter = "";
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now().withNano(0);
    }
}

