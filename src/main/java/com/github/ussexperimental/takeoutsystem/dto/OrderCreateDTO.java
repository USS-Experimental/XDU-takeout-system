package com.github.ussexperimental.takeoutsystem.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderCreateDTO {
    private Long customerId;
    private Long merchantId;
    private List<Long> dishIds;
    private Date deliveryTime;
    private String deliveryLocation;
}
