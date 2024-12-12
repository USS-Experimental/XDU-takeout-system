package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {

    Order changeOrderStatus(Long orderId, OrderStatus newStatus);

    Order getOrderDetails(Long orderId);
}
