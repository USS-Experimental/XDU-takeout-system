package com.github.ussexperimental.takeoutsystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class Customer extends User {

    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
}
