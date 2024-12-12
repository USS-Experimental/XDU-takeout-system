package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.*;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import com.github.ussexperimental.takeoutsystem.repository.*;
import com.github.ussexperimental.takeoutsystem.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerServiceTest {

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 1. 测试 viewMenu 方法

    @Test
    @DisplayName("测试查看菜单 - 成功")
    public void testViewMenu_Success() {
        // 准备数据
        Long merchantId = 1L;
        int page = 0;
        int size = 10;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        Dish dish1 = new Dish();
        dish1.setId(1L);
        dish1.setName("宫保鸡丁");
        dish1.setPrice(new BigDecimal("25.50"));
        dish1.setMerchant(merchant);

        Dish dish2 = new Dish();
        dish2.setId(2L);
        dish2.setName("麻婆豆腐");
        dish2.setPrice(new BigDecimal("20.00"));
        dish2.setMerchant(merchant);

        List<Dish> dishes = Arrays.asList(dish1, dish2);
        Page<Dish> dishPage = new PageImpl<>(dishes, PageRequest.of(page, size), dishes.size());

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(dishRepository.findByMerchant(merchant, PageRequest.of(page, size, Sort.by("id").descending())))
                .thenReturn(dishPage);

        // 调用方法
        PageResponse<Dish> response = customerService.viewMenu(merchantId, page, size);

        // 验证
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(dishRepository, times(1)).findByMerchant(merchant, PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @Test
    @DisplayName("测试查看菜单 - 商家不存在")
    public void testViewMenu_MerchantNotFound() {
        // 准备数据
        Long merchantId = 100L;
        int page = 0;
        int size = 10;

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.viewMenu(merchantId, page, size);
        });

        assertEquals("商家不存在", exception.getMessage());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(dishRepository, never()).findByMerchant(any(Merchant.class), any(Pageable.class));
    }

    // 2. 测试 createOrder 方法

    @Test
    @DisplayName("测试创建订单 - 成功")
    public void testCreateOrder_Success() {
        // 准备数据
        Long customerId = 1L;
        Long merchantId = 2L;
        List<Long> dishIds = Arrays.asList(1L, 2L);
        Date deliveryTime = new GregorianCalendar(2024, Calendar.DECEMBER, 25, 18, 0).getTime();
        String deliveryLocation = "123 Main St";

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        Dish dish1 = new Dish();
        dish1.setId(1L);
        dish1.setName("宫保鸡丁");
        dish1.setPrice(new BigDecimal("25.50"));
        dish1.setMerchant(merchant);

        Dish dish2 = new Dish();
        dish2.setId(2L);
        dish2.setName("麻婆豆腐");
        dish2.setPrice(new BigDecimal("20.00"));
        dish2.setMerchant(merchant);

        List<Dish> dishes = Arrays.asList(dish1, dish2);
        BigDecimal totalPrice = new BigDecimal("45.50");

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setMerchant(merchant);
        order.setDishes(dishes);
        order.setDeliveryTime(deliveryTime);
        order.setDeliveryLocation(deliveryLocation);
        order.setOrderTime(new Date());
        order.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order.setTotalPrice(totalPrice);

        // 模拟仓库行为
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(dishRepository.findAllById(dishIds)).thenReturn(dishes);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        // 调用方法
        Order createdOrder = customerService.createOrder(customerId, merchantId, dishIds, deliveryTime, deliveryLocation);

        // 验证
        assertNotNull(createdOrder);
        assertEquals(1L, createdOrder.getId());
        assertEquals(customer, createdOrder.getCustomer());
        assertEquals(merchant, createdOrder.getMerchant());
        assertEquals(dishes, createdOrder.getDishes());
        assertEquals(deliveryTime, createdOrder.getDeliveryTime());
        assertEquals(deliveryLocation, createdOrder.getDeliveryLocation());
        assertEquals(OrderStatus.PENDING_CONFIRMATION, createdOrder.getStatus());
        assertEquals(totalPrice, createdOrder.getTotalPrice());

        verify(customerRepository, times(1)).findById(customerId);
        verify(merchantRepository, times(1)).findById(merchantId);
        verify(dishRepository, times(1)).findAllById(dishIds);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("测试创建订单 - 顾客不存在")
    public void testCreateOrder_CustomerNotFound() {
        // 准备数据
        Long customerId = 100L;
        Long merchantId = 2L;
        List<Long> dishIds = Arrays.asList(1L, 2L);
        Date deliveryTime = new Date();
        String deliveryLocation = "123 Main St";

        // 模拟仓库行为
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.createOrder(customerId, merchantId, dishIds, deliveryTime, deliveryLocation);
        });

        assertEquals("顾客不存在", exception.getMessage());

        verify(customerRepository, times(1)).findById(customerId);
        verify(merchantRepository, never()).findById(anyLong());
        verify(dishRepository, never()).findAllById(anyList());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试创建订单 - 商家不存在")
    public void testCreateOrder_MerchantNotFound() {
        // 准备数据
        Long customerId = 1L;
        Long merchantId = 100L;
        List<Long> dishIds = Arrays.asList(1L, 2L);
        Date deliveryTime = new Date();
        String deliveryLocation = "123 Main St";

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        // 模拟仓库行为
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.createOrder(customerId, merchantId, dishIds, deliveryTime, deliveryLocation);
        });

        assertEquals("商家不存在", exception.getMessage());

        verify(customerRepository, times(1)).findById(customerId);
        verify(merchantRepository, times(1)).findById(merchantId);
        verify(dishRepository, never()).findAllById(anyList());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试创建订单 - 菜品不存在")
    public void testCreateOrder_DishesNotFound() {
        // 准备数据
        Long customerId = 1L;
        Long merchantId = 2L;
        List<Long> dishIds = Arrays.asList(100L, 200L);
        Date deliveryTime = new Date();
        String deliveryLocation = "123 Main St";

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        // 模拟仓库行为
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(dishRepository.findAllById(dishIds)).thenReturn(Collections.emptyList());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.createOrder(customerId, merchantId, dishIds, deliveryTime, deliveryLocation);
        });

        assertEquals("菜品不存在", exception.getMessage());

        verify(customerRepository, times(1)).findById(customerId);
        verify(merchantRepository, times(1)).findById(merchantId);
        verify(dishRepository, times(1)).findAllById(dishIds);
        verify(orderRepository, never()).save(any(Order.class));
    }

    // 3. 测试 getMyOrders 方法

    @Test
    @DisplayName("测试获取顾客的所有订单 - 成功")
    public void testGetMyOrders_Success() {
        // 准备数据
        Long customerId = 1L;
        int page = 0;
        int size = 10;

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setCustomer(customer);
        order1.setStatus(OrderStatus.PENDING_CONFIRMATION);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomer(customer);
        order2.setStatus(OrderStatus.DELIVERED);

        List<Order> orders = Arrays.asList(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(page, size), orders.size());

        // 模拟仓库行为
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer, PageRequest.of(page, size, Sort.by("orderTime").descending())))
                .thenReturn(orderPage);

        // 调用方法
        PageResponse<Order> response = customerService.getMyOrders(customerId, page, size);

        // 验证
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(customerRepository, times(1)).findById(customerId);
        verify(orderRepository, times(1)).findByCustomer(customer, PageRequest.of(page, size, Sort.by("orderTime").descending()));
    }

    @Test
    @DisplayName("测试获取顾客的所有订单 - 顾客不存在")
    public void testGetMyOrders_CustomerNotFound() {
        // 准备数据
        Long customerId = 100L;
        int page = 0;
        int size = 10;

        // 模拟仓库行为
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.getMyOrders(customerId, page, size);
        });

        assertEquals("顾客不存在", exception.getMessage());

        verify(customerRepository, times(1)).findById(customerId);
        verify(orderRepository, never()).findByCustomer(any(Customer.class), any(Pageable.class));
    }

    // 4. 测试 getOrderDetails 方法

    @Test
    @DisplayName("测试获取订单详情 - 成功")
    public void testGetOrderDetails_Success() {
        // 准备数据
        Long customerId = 1L;
        Long orderId = 1L;

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.DELIVERED);

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法
        Order foundOrder = customerService.getOrderDetails(customerId, orderId);

        // 验证
        assertNotNull(foundOrder);
        assertEquals(orderId, foundOrder.getId());
        assertEquals(customer, foundOrder.getCustomer());

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("测试获取订单详情 - 订单不存在")
    public void testGetOrderDetails_OrderNotFound() {
        // 准备数据
        Long customerId = 1L;
        Long orderId = 100L;

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.getOrderDetails(customerId, orderId);
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("测试获取订单详情 - 无权访问")
    public void testGetOrderDetails_UnauthorizedAccess() {
        // 准备数据
        Long customerId = 1L;
        Long orderId = 1L;

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        otherCustomer.setUsername("customer2");

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(otherCustomer);
        order.setStatus(OrderStatus.DELIVERED);

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.getOrderDetails(customerId, orderId);
        });

        assertEquals("无权访问该订单", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
    }

    // 5. 测试 reviewOrder 方法

    @Test
    @DisplayName("测试评价订单 - 成功（新评价）")
    public void testReviewOrder_NewReview_Success() {
        // 准备数据
        Long customerId = 1L;
        Long orderId = 1L;
        int rating = 5;
        String comment = "非常满意！";

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.DELIVERED);
        order.setReview(null);

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(1L); // 为 Review 设置一个模拟的 ID
            return r;
        });
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // 调用方法
        Review createdReview = customerService.reviewOrder(customerId, orderId, rating, comment);

        // 验证
        assertNotNull(createdReview);
        assertEquals(1L, createdReview.getId());
        assertEquals(rating, createdReview.getRating());
        assertEquals(comment, createdReview.getComment());
        assertNotNull(createdReview.getReviewTime());

        assertEquals(OrderStatus.REVIEWED, order.getStatus());
        assertEquals(createdReview, order.getReview());

        verify(orderRepository, times(1)).findById(orderId);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(orderRepository, times(1)).save(order);
    }


    @Test
    @DisplayName("测试评价订单 - 成功（更新已有评价）")
    public void testReviewOrder_UpdateReview_Success() {
        // 准备数据
        Long customerId = 1L;
        Long orderId = 1L;
        int rating = 4;
        String comment = "总体满意。";

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.DELIVERED);

        Review existingReview = new Review();
        existingReview.setId(1L);
        existingReview.setOrder(order);
        existingReview.setRating(5);
        existingReview.setComment("非常满意！");
        existingReview.setReviewTime(new Date());

        order.setReview(existingReview);
        order.setStatus(OrderStatus.REVIEWED);

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(reviewRepository.save(existingReview)).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setRating(rating);
            r.setComment(comment);
            r.setReviewTime(new Date());
            return r;
        });
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // 调用方法
        Review resultReview = customerService.reviewOrder(customerId, orderId, rating, comment);

        // 验证
        assertNotNull(resultReview);
        assertEquals(rating, resultReview.getRating());
        assertEquals(comment, resultReview.getComment());
        assertNotNull(resultReview.getReviewTime());

        assertEquals(OrderStatus.REVIEWED, order.getStatus());
        assertEquals(resultReview, order.getReview());

        verify(orderRepository, times(1)).findById(orderId);
        verify(reviewRepository, times(1)).save(existingReview);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("测试评价订单 - 订单不存在")
    public void testReviewOrder_OrderNotFound() {
        // 准备数据
        Long customerId = 1L;
        Long orderId = 100L;
        int rating = 5;
        String comment = "非常满意！";

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.reviewOrder(customerId, orderId, rating, comment);
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试评价订单 - 无权评价该订单")
    public void testReviewOrder_UnauthorizedAccess() {
        // 准备数据
        Long customerId = 1L;
        Long orderId = 1L;
        int rating = 5;
        String comment = "非常满意！";

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        otherCustomer.setUsername("customer2");

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(otherCustomer);
        order.setStatus(OrderStatus.DELIVERED);

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.reviewOrder(customerId, orderId, rating, comment);
        });

        assertEquals("无权评价该订单", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试评价订单 - 订单尚未送达，无法评价")
    public void testReviewOrder_OrderNotDelivered() {
        // 准备数据
        Long customerId = 1L;
        Long orderId = 1L;
        int rating = 5;
        String comment = "非常满意！";

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PREPARING); // 订单状态为 PREPARING

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.reviewOrder(customerId, orderId, rating, comment);
        });

        assertEquals("订单尚未送达，无法评价", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    // 6. 其他测试方法根据需要添加

    // 例如，更多的测试用例可以覆盖边界情况和异常情况
}
