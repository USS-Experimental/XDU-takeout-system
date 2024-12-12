package com.github.ussexperimental.takeoutsystem.service.impl;

import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import com.github.ussexperimental.takeoutsystem.repository.DeliveryManRepository;
import com.github.ussexperimental.takeoutsystem.repository.OrderRepository;
import com.github.ussexperimental.takeoutsystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryManRepository deliveryManRepository;

    // 更改订单状态
    @Transactional
    public Order changeOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    // 获取订单详情
    public Order getOrderDetails(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    }
}
