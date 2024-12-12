package com.github.ussexperimental.takeoutsystem.service.impl;

import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.Review;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import com.github.ussexperimental.takeoutsystem.repository.OrderRepository;
import com.github.ussexperimental.takeoutsystem.repository.ReviewRepository;
import com.github.ussexperimental.takeoutsystem.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Review createOrUpdateReview(Long customerId, Long orderId, int rating, String comment) {
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
            review.setReviewTime(new java.util.Date());
        } else {
            review = new Review();
            review.setOrder(order);
            review.setRating(rating);
            review.setComment(comment);
            review.setReviewTime(new java.util.Date());
            order.setReview(review);
            order.setStatus(OrderStatus.REVIEWED);
        }

        reviewRepository.save(review);
        orderRepository.save(order);

        return review;
    }

    // 获取评价
    public Review getReviewByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (order.getReview() == null) {
            throw new IllegalArgumentException("该订单尚未评价");
        }

        return order.getReview();
    }
}
