package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.Dish;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public interface MerchantService {

    Dish addDish(Long merchantId,
                 String name,
                 BigDecimal price,
                 String description,
                 String imageUrl);

    PageResponse<Dish> getMenu(Long merchantId, int page, int size);

    Dish updateDish(Long dishId,
                    String name,
                    BigDecimal price,
                    String description,
                    String imageUrl);

    void deleteDish(Long dishId);

    PageResponse<Order> viewSales(Long merchantId,
                                  Date startDate,
                                  Date endDate,
                                  int page,
                                  int size);

    PageResponse<Order> viewPendingOrders(Long merchantId, int page, int size);

    Order acceptOrder(Long merchantId, Long orderId);

    Order requestDelivery(Long orderId, Long deliveryManId);
}
