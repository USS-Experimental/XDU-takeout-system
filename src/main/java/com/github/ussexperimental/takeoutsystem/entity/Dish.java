package com.github.ussexperimental.takeoutsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable=false)
    private Merchant merchant;

    @Column(nullable = false)
    private String name;

    private BigDecimal price;

    private String description;

    private String imageUrl;
}

