package com.github.ussexperimental.takeoutsystem.controller;

import com.github.ussexperimental.takeoutsystem.dto.OrderCreateDTO;
import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.dto.ReviewDTO;
import com.github.ussexperimental.takeoutsystem.entity.Dish;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.Review;
import com.github.ussexperimental.takeoutsystem.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 查看菜单
     * GET /customers/menu?page={page}&size={size}
     */
    @GetMapping("/menu")
    public ResponseEntity<PageResponse<Dish>> viewMenu(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PageResponse<Dish> menu = customerService.viewMenu(merchantId, page, size);
            if (menu.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(menu, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 创建订单
     * POST /customers/orders
     */
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        try {
            Order order = customerService.createOrder(
                    orderCreateDTO.getCustomerId(),
                    orderCreateDTO.getMerchantId(),
                    orderCreateDTO.getDishIds(),
                    orderCreateDTO.getDeliveryTime(),
                    orderCreateDTO.getDeliveryLocation()
            );
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 查看顾客所有订单
     * GET /customers/orders?customerId={customerId}&page={page}&size={size}
     */
    @GetMapping("/orders")
    public ResponseEntity<PageResponse<Order>> getMyOrders(
            @RequestParam Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PageResponse<Order> orders = customerService.getMyOrders(customerId, page, size);
            if (orders.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 查看单个订单详情
     * GET /customers/orders/{orderId}?customerId={customerId}
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrderDetails(
            @PathVariable Long orderId,
            @RequestParam Long customerId
    ) {
        try {
            Order order = customerService.getOrderDetails(customerId, orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 评价订单
     * POST /customers/orders/{orderId}/reviews?customerId={customerId}
     */
    @PostMapping("/orders/{orderId}/reviews")
    public ResponseEntity<Review> reviewOrder(
            @PathVariable Long orderId,
            @RequestParam Long customerId,
            @RequestBody ReviewDTO   reviewDTO
    ) {
        try {
            Review review = customerService.reviewOrder(
                    customerId,
                    orderId,
                    reviewDTO.getRating(),
                    reviewDTO.getComment()
            );
            return new ResponseEntity<>(review, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
