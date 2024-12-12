package com.github.ussexperimental.takeoutsystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class Merchant extends User {

    private String merchantName;

    @OneToMany(mappedBy = "merchant")
    private List<Dish> dishes;
}

