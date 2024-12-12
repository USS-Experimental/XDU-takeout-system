package com.github.ussexperimental.takeoutsystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishUpdateDTO {
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
}
