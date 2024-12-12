package com.github.ussexperimental.takeoutsystem.service.impl;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.DeliveryMan;
import com.github.ussexperimental.takeoutsystem.entity.Dish;
import com.github.ussexperimental.takeoutsystem.entity.Merchant;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import com.github.ussexperimental.takeoutsystem.repository.DeliveryManRepository;
import com.github.ussexperimental.takeoutsystem.repository.DishRepository;
import com.github.ussexperimental.takeoutsystem.repository.MerchantRepository;
import com.github.ussexperimental.takeoutsystem.repository.OrderRepository;
import com.github.ussexperimental.takeoutsystem.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryManRepository deliveryManRepository;

    @Transactional
    public Dish addDish(Long merchantId, String name, BigDecimal price, String description, String imageUrl) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));

        Dish dish = new Dish();
        dish.setMerchant(merchant);
        dish.setName(name);
        dish.setPrice(price);
        dish.setDescription(description);
        dish.setImageUrl(imageUrl);

        return dishRepository.save(dish);
    }

    public PageResponse<Dish> getMenu(Long merchantId, int page, int size) {
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
    public Dish updateDish(Long dishId, String name, BigDecimal price, String description, String imageUrl) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("菜品不存在"));

        if (name != null && !name.isEmpty()) {
            dish.setName(name);
        }
        if (price != null) {
            dish.setPrice(price);
        }
        if (description != null && !description.isEmpty()) {
            dish.setDescription(description);
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            dish.setImageUrl(imageUrl);
        }

        return dishRepository.save(dish);
    }

    @Transactional
    public void deleteDish(Long dishId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("菜品不存在"));

        dishRepository.delete(dish);
    }

    public PageResponse<Order> viewSales(Long merchantId, Date startDate, Date endDate, int page, int size) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderTime").descending());

        Page<Order> orderPage;
        if (startDate != null && endDate != null) {
            orderPage = orderRepository.findByMerchantAndOrderTimeBetween(merchant, startDate, endDate, pageable);
        } else {
            orderPage = orderRepository.findByMerchant(merchant, pageable);
        }

        return new PageResponse<>(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }

    public PageResponse<Order> viewPendingOrders(Long merchantId, int page, int size) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderTime").descending());
        Page<Order> pendingOrders = orderRepository.findByMerchantAndStatus(merchant, OrderStatus.PENDING_CONFIRMATION, pageable);

        return new PageResponse<>(
                pendingOrders.getContent(),
                pendingOrders.getNumber(),
                pendingOrders.getSize(),
                pendingOrders.getTotalElements(),
                pendingOrders.getTotalPages(),
                pendingOrders.isLast()
        );
    }

    @Transactional
    public Order acceptOrder(Long merchantId, Long orderId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getMerchant().getId().equals(merchantId)) {
            throw new IllegalArgumentException("无权接受该订单");
        }

        if (order.getStatus() != OrderStatus.PENDING_CONFIRMATION) {
            throw new IllegalArgumentException("订单当前状态无法接受");
        }

        order.setStatus(OrderStatus.PREPARING);
        return orderRepository.save(order);
    }

    @Transactional
    public Order requestDelivery(Long orderId, Long deliveryManId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalArgumentException("订单当前状态无法请求送餐");
        }

        DeliveryMan deliveryMan = deliveryManRepository.findById(deliveryManId)
                .orElseThrow(() -> new IllegalArgumentException("送餐员不存在"));

        order.setDeliveryMan(deliveryMan);
        order.setStatus(OrderStatus.DELIVERING);
        return orderRepository.save(order);
    }
}
