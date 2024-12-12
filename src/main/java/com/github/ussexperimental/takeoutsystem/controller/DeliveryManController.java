package com.github.ussexperimental.takeoutsystem.controller;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.service.DeliveryManService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliverymen")
public class DeliveryManController {

    @Autowired
    private DeliveryManService deliveryManService;

    /**
     * 查看可接单列表
     * GET /deliverymen/orders/available?deliveryManId={deliveryManId}&page={page}&size={size}
     *
     * @param deliveryManId 外卖员ID
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @return 可接单的订单列表
     */
    @GetMapping("/orders/available")
    public ResponseEntity<PageResponse<Order>> viewAvailableOrders(
            @RequestParam Long deliveryManId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PageResponse<Order> availableOrders = deliveryManService.viewAvailableOrders(deliveryManId, page, size);
            if (availableOrders.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(availableOrders, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 确认取餐，将订单状态更新为“送餐中”
     * PUT /deliverymen/orders/{orderId}/pickup?deliveryManId={deliveryManId}
     *
     * @param orderId        订单ID
     * @param deliveryManId 外卖员ID
     * @return 更新后的订单信息
     */
    @PutMapping("/orders/{orderId}/pickup")
    public ResponseEntity<Order> confirmPickup(
            @PathVariable Long orderId,
            @RequestParam Long deliveryManId
    ) {
        try {
            Order order = deliveryManService.confirmPickup(deliveryManId, orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 确认送达，将订单状态更新为“已送达”
     * PUT /deliverymen/orders/{orderId}/deliver?deliveryManId={deliveryManId}
     *
     * @param orderId        订单ID
     * @param deliveryManId 外卖员ID
     * @return 更新后的订单信息
     */
    @PutMapping("/orders/{orderId}/deliver")
    public ResponseEntity<Order> confirmDelivery(
            @PathVariable Long orderId,
            @RequestParam Long deliveryManId
    ) {
        try {
            Order order = deliveryManService.confirmDelivery(deliveryManId, orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 查看我的送餐订单
     * GET /deliverymen/orders?deliveryManId={deliveryManId}&page={page}&size={size}
     *
     * @param deliveryManId 外卖员ID
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @return 当前外卖员的所有送餐订单
     */
    @GetMapping("/orders")
    public ResponseEntity<PageResponse<Order>> viewMyDeliveries(
            @RequestParam Long deliveryManId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PageResponse<Order> deliveries = deliveryManService.viewMyDeliveries(deliveryManId, page, size);
            if (deliveries.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(deliveries, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
