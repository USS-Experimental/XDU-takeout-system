package com.github.ussexperimental.takeoutsystem.service.impl;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.*;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import com.github.ussexperimental.takeoutsystem.repository.*;
import com.github.ussexperimental.takeoutsystem.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * 查看菜单，支持分页
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页的菜单列表
     */
    public PageResponse<Dish> viewMenu(Long merchantId, int page, int size) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Dish> dishPage = dishRepository.findByMerchant(merchant, pageable);

        return new PageResponse<>(
                dishPage.getContent(),
                dishPage.getNumber(),
                dishPage.getSize(),
                dishPage.getTotalElements(),
                dishPage.getTotalPages(),
                dishPage.isLast()
        );
    }

    @Transactional
    public Order createOrder(Long customerId, Long merchantId, List<Long> dishIds, Date deliveryTime, String deliveryLocation) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("顾客不存在"));

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));

        List<Dish> dishes = dishRepository.findAllById(dishIds);
        if (dishes.isEmpty()) {
            throw new IllegalArgumentException("菜品不存在");
        }

        // 计算总价
        BigDecimal totalPrice = dishes.stream()
                .map(Dish::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setCustomer(customer);
        order.setMerchant(merchant);
        order.setDishes(dishes);
        order.setDeliveryTime(deliveryTime);
        order.setDeliveryLocation(deliveryLocation);
        order.setOrderTime(new Date());
        order.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }

    /**
     * 获取顾客的所有订单，支持分页
     * @param customerId 顾客ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页的订单列表
     */
    public PageResponse<Order> getMyOrders(Long customerId, int page, int size) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("顾客不存在"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderTime").descending());
        Page<Order> orderPage = orderRepository.findByCustomer(customer, pageable);

        return new PageResponse<>(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }

    public Order getOrderDetails(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("无权访问该订单");
        }

        return order;
    }

    @Transactional
    public Review reviewOrder(Long customerId, Long orderId, int rating, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("无权评价该订单");
        }

        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.REVIEWED) {
            throw new IllegalArgumentException("订单尚未送达，无法评价");
        }

        Review review;
        if (order.getReview() != null) {
            review = order.getReview();
            review.setRating(rating);
            review.setComment(comment);
            review.setReviewTime(new Date());
        } else {
            review = new Review();
            review.setOrder(order);
            review.setRating(rating);
            review.setComment(comment);
            review.setReviewTime(new Date());
            order.setReview(review);
            order.setStatus(OrderStatus.REVIEWED);
        }

        reviewRepository.save(review);
        orderRepository.save(order);

        return review;
    }
}
