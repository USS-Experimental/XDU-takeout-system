package com.github.ussexperimental.takeoutsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable=false)
    private Order order;

    private int rating;

    private String comment;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewTime;
}

