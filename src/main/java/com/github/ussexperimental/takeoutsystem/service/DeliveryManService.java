package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import org.springframework.stereotype.Service;

@Service
public interface DeliveryManService {

    PageResponse<Order> viewAvailableOrders(Long deliveryManId, int page, int size);

    Order confirmPickup(Long deliveryManId, Long orderId);

    Order confirmDelivery(Long deliveryManId, Long orderId);

    PageResponse<Order> viewMyDeliveries(Long deliveryManId, int page, int size);
}
