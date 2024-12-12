package com.github.ussexperimental.takeoutsystem.service.impl;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.DeliveryMan;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import com.github.ussexperimental.takeoutsystem.repository.DeliveryManRepository;
import com.github.ussexperimental.takeoutsystem.repository.OrderRepository;
import com.github.ussexperimental.takeoutsystem.service.DeliveryManService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryManServiceImpl implements DeliveryManService {

    @Autowired
    private DeliveryManRepository deliveryManRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 查看可接单列表，获取所有状态为 REQUESTING_DELIVERY 且未分配送餐员的订单
     *
     * @param deliveryManId 外卖员ID
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @return 可接单的订单分页数据
     */
    public PageResponse<Order> viewAvailableOrders(Long deliveryManId, int page, int size) {
        // 验证外卖员是否存在
        DeliveryMan deliveryMan = deliveryManRepository.findById(deliveryManId)
                .orElseThrow(() -> new IllegalArgumentException("外卖员不存在"));

        // 定义分页请求，按订单时间降序排序
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderTime").descending());

        // 查询状态为 REQUESTING_DELIVERY 且 deliveryMan 为空的订单
        Page<Order> orderPage = orderRepository.findByStatusAndDeliveryManIsNull(OrderStatus.REQUESTING_DELIVERY, pageable);

        return new PageResponse<>(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }

    // 确认取餐
    @Transactional
    public Order confirmPickup(Long deliveryManId, Long orderId) {
        DeliveryMan deliveryMan = deliveryManRepository.findById(deliveryManId)
                .orElseThrow(() -> new IllegalArgumentException("外卖员不存在"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (order.getStatus() != OrderStatus.REQUESTING_DELIVERY) {
            throw new IllegalArgumentException("订单当前状态无法取餐");
        }

        if (order.getDeliveryMan() != null) {
            throw new IllegalArgumentException("订单已被其他外卖员接单");
        }

        order.setDeliveryMan(deliveryMan);
        order.setStatus(OrderStatus.DELIVERING);
        return orderRepository.save(order);
    }

    // 确认送达
    @Transactional
    public Order confirmDelivery(Long deliveryManId, Long orderId) {
        DeliveryMan deliveryMan = deliveryManRepository.findById(deliveryManId)
                .orElseThrow(() -> new IllegalArgumentException("外卖员不存在"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new IllegalArgumentException("订单当前状态无法确认送达");
        }

        if (!order.getDeliveryMan().getId().equals(deliveryManId)) {
            throw new IllegalArgumentException("该订单不属于当前外卖员");
        }

        order.setStatus(OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }

    /**
     * 查看我的送餐订单，获取所有分配给当前外卖员的订单，支持分页
     *
     * @param deliveryManId 外卖员ID
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @return 当前外卖员的所有送餐订单分页数据
     */
    public PageResponse<Order> viewMyDeliveries(Long deliveryManId, int page, int size) {
        // 验证外卖员是否存在
        DeliveryMan deliveryMan = deliveryManRepository.findById(deliveryManId)
                .orElseThrow(() -> new IllegalArgumentException("外卖员不存在"));

        // 定义分页请求，按订单时间降序排序
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderTime").descending());

        // 查询所有分配给当前外卖员的订单
        Page<Order> orderPage = orderRepository.findByDeliveryMan(deliveryMan, pageable);

        return new PageResponse<>(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }
}
