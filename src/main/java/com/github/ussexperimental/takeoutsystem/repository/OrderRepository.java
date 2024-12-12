package com.github.ussexperimental.takeoutsystem.repository;

import com.github.ussexperimental.takeoutsystem.entity.Customer;
import com.github.ussexperimental.takeoutsystem.entity.DeliveryMan;
import com.github.ussexperimental.takeoutsystem.entity.Merchant;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer(Customer customer);

    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    List<Order> findByCustomer_Id(Long customerId);

    Page<Order> findByMerchant(Merchant merchant, Pageable pageable);

    Page<Order> findByDeliveryMan(DeliveryMan deliveryMan, Pageable pageable);

    List<Order> findOrdersByStatus(OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    // 查询状态为特定值且未分配送餐员的订单
    Page<Order> findByStatusAndDeliveryManIsNull(OrderStatus status, Pageable pageable);

    // 支持查看商家在特定时间范围内的订单
    Page<Order> findByMerchantAndOrderTimeBetween(Merchant merchant, Date startDate, Date endDate, Pageable pageable);

    // 支持查看商家待确认的订单
    Page<Order> findByMerchantAndStatus(Merchant merchant, OrderStatus status, Pageable pageable);
}

