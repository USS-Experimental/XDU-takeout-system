package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.Dish;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.Review;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface CustomerService {

    PageResponse<Dish> viewMenu(Long merchantId, int page, int size);

    Order createOrder(Long customerId,
                      Long merchantId,
                      List<Long> dishIds,
                      Date deliveryTime,
                      String deliveryLocation);

    PageResponse<Order> getMyOrders(Long customerId, int page, int size);

    Order getOrderDetails(Long customerId, Long orderId);

    Review reviewOrder(Long customerId, Long orderId, int rating, String comment);
}
