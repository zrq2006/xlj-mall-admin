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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "market_price", nullable = false)
    private BigDecimal marketPrice;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer sales;

    private String image;

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
        if (price == null) price = BigDecimal.ZERO;
        if (marketPrice == null) marketPrice = BigDecimal.ZERO;
        if (stock == null) stock = 0;
        if (sales == null) sales = 0;
        if (status == null) status = 1;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now().withNano(0);
    }
}

