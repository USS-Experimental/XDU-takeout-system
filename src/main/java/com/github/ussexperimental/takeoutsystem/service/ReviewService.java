package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.entity.Review;
import org.springframework.stereotype.Service;

@Service
public interface ReviewService {

    Review createOrUpdateReview(Long customerId,
                                Long orderId,
                                int rating,
                                String comment);

    Review getReviewByOrderId(Long orderId);
}
