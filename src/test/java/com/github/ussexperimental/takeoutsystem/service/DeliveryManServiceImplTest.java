package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.DeliveryMan;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import com.github.ussexperimental.takeoutsystem.repository.DeliveryManRepository;
import com.github.ussexperimental.takeoutsystem.repository.OrderRepository;
import com.github.ussexperimental.takeoutsystem.service.impl.DeliveryManServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeliveryManServiceImplTest {

    @InjectMocks
    private DeliveryManServiceImpl deliveryManService;

    @Mock
    private DeliveryManRepository deliveryManRepository;

    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 1. 测试 viewAvailableOrders 方法

    @Test
    @DisplayName("测试查看可接单列表 - 成功")
    public void testViewAvailableOrders_Success() {
        // 准备数据
        Long deliveryManId = 1L;
        int page = 0;
        int size = 10;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.REQUESTING_DELIVERY);
        order1.setDeliveryMan(null);
        order1.setOrderTime(new Date());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(OrderStatus.REQUESTING_DELIVERY);
        order2.setDeliveryMan(null);
        order2.setOrderTime(new Date());

        List<Order> availableOrders = Arrays.asList(order1, order2);
        Page<Order> orderPage = new PageImpl<>(availableOrders, PageRequest.of(page, size, Sort.by("orderTime").descending()), availableOrders.size());

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findByStatusAndDeliveryManIsNull(OrderStatus.REQUESTING_DELIVERY, PageRequest.of(page, size, Sort.by("orderTime").descending())))
                .thenReturn(orderPage);

        // 调用方法
        PageResponse<Order> response = deliveryManService.viewAvailableOrders(deliveryManId, page, size);

        // 验证
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findByStatusAndDeliveryManIsNull(OrderStatus.REQUESTING_DELIVERY, PageRequest.of(page, size, Sort.by("orderTime").descending()));
    }

    @Test
    @DisplayName("测试查看可接单列表 - 外卖员不存在")
    public void testViewAvailableOrders_DeliveryManNotFound() {
        // 准备数据
        Long deliveryManId = 100L;
        int page = 0;
        int size = 10;

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.viewAvailableOrders(deliveryManId, page, size);
        });

        assertEquals("外卖员不存在", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, never()).findByStatusAndDeliveryManIsNull(any(OrderStatus.class), any(Pageable.class));
    }

    // 2. 测试 confirmPickup 方法

    @Test
    @DisplayName("测试确认取餐 - 成功")
    public void testConfirmPickup_Success() {
        // 准备数据
        Long deliveryManId = 1L;
        Long orderId = 1L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.REQUESTING_DELIVERY);
        order.setDeliveryMan(null);
        order.setOrderTime(new Date());

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.DELIVERING);
        updatedOrder.setDeliveryMan(deliveryMan);
        updatedOrder.setOrderTime(order.getOrderTime());

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(updatedOrder);

        // 调用方法
        Order result = deliveryManService.confirmPickup(deliveryManId, orderId);

        // 验证
        assertNotNull(result);
        assertEquals(OrderStatus.DELIVERING, result.getStatus());
        assertEquals(deliveryMan, result.getDeliveryMan());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("测试确认取餐 - 外卖员不存在")
    public void testConfirmPickup_DeliveryManNotFound() {
        // 准备数据
        Long deliveryManId = 100L;
        Long orderId = 1L;

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.confirmPickup(deliveryManId, orderId);
        });

        assertEquals("外卖员不存在", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试确认取餐 - 订单不存在")
    public void testConfirmPickup_OrderNotFound() {
        // 准备数据
        Long deliveryManId = 1L;
        Long orderId = 100L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.confirmPickup(deliveryManId, orderId);
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试确认取餐 - 订单状态不正确")
    public void testConfirmPickup_InvalidOrderStatus() {
        // 准备数据
        Long deliveryManId = 1L;
        Long orderId = 1L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PREPARING); // 不等于 REQUESTING_DELIVERY
        order.setDeliveryMan(null);
        order.setOrderTime(new Date());

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.confirmPickup(deliveryManId, orderId);
        });

        assertEquals("订单当前状态无法取餐", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    // 3. 测试 confirmDelivery 方法

    @Test
    @DisplayName("测试确认送达 - 成功")
    public void testConfirmDelivery_Success() {
        // 准备数据
        Long deliveryManId = 1L;
        Long orderId = 1L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.DELIVERING);
        order.setDeliveryMan(deliveryMan);
        order.setOrderTime(new Date());

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.DELIVERED);
        updatedOrder.setDeliveryMan(deliveryMan);
        updatedOrder.setOrderTime(order.getOrderTime());

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(updatedOrder);

        // 调用方法
        Order result = deliveryManService.confirmDelivery(deliveryManId, orderId);

        // 验证
        assertNotNull(result);
        assertEquals(OrderStatus.DELIVERED, result.getStatus());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("测试确认送达 - 外卖员不存在")
    public void testConfirmDelivery_DeliveryManNotFound() {
        // 准备数据
        Long deliveryManId = 100L;
        Long orderId = 1L;

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.confirmDelivery(deliveryManId, orderId);
        });

        assertEquals("外卖员不存在", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试确认送达 - 订单不存在")
    public void testConfirmDelivery_OrderNotFound() {
        // 准备数据
        Long deliveryManId = 1L;
        Long orderId = 100L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.confirmDelivery(deliveryManId, orderId);
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试确认送达 - 订单状态不正确")
    public void testConfirmDelivery_InvalidOrderStatus() {
        // 准备数据
        Long deliveryManId = 1L;
        Long orderId = 1L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PREPARING); // 不等于 DELIVERING
        order.setDeliveryMan(deliveryMan);
        order.setOrderTime(new Date());

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.confirmDelivery(deliveryManId, orderId);
        });

        assertEquals("订单当前状态无法确认送达", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试确认送达 - 外卖员与订单不匹配")
    public void testConfirmDelivery_UnauthorizedDeliveryMan() {
        // 准备数据
        Long deliveryManId = 1L;
        Long orderId = 1L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        DeliveryMan otherDeliveryMan = new DeliveryMan();
        otherDeliveryMan.setId(2L);
        otherDeliveryMan.setUsername("delivery2");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.DELIVERING);
        order.setDeliveryMan(otherDeliveryMan);
        order.setOrderTime(new Date());

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.confirmDelivery(deliveryManId, orderId);
        });

        assertEquals("该订单不属于当前外卖员", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    // 4. 测试 viewMyDeliveries 方法

    @Test
    @DisplayName("测试查看我的送餐订单 - 成功")
    public void testViewMyDeliveries_Success() {
        // 准备数据
        Long deliveryManId = 1L;
        int page = 0;
        int size = 10;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setDeliveryMan(deliveryMan);
        order1.setStatus(OrderStatus.DELIVERING);
        order1.setOrderTime(new Date());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setDeliveryMan(deliveryMan);
        order2.setStatus(OrderStatus.DELIVERED);
        order2.setOrderTime(new Date());

        List<Order> myDeliveries = Arrays.asList(order1, order2);
        Page<Order> orderPage = new PageImpl<>(myDeliveries, PageRequest.of(page, size, Sort.by("orderTime").descending()), myDeliveries.size());

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findByDeliveryMan(deliveryMan, PageRequest.of(page, size, Sort.by("orderTime").descending())))
                .thenReturn(orderPage);

        // 调用方法
        PageResponse<Order> response = deliveryManService.viewMyDeliveries(deliveryManId, page, size);

        // 验证
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findByDeliveryMan(deliveryMan, PageRequest.of(page, size, Sort.by("orderTime").descending()));
    }

    @Test
    @DisplayName("测试查看我的送餐订单 - 外卖员不存在")
    public void testViewMyDeliveries_DeliveryManNotFound() {
        // 准备数据
        Long deliveryManId = 100L;
        int page = 0;
        int size = 10;

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.viewMyDeliveries(deliveryManId, page, size);
        });

        assertEquals("外卖员不存在", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, never()).findByDeliveryMan(any(DeliveryMan.class), any(Pageable.class));
    }

    // 5. 测试 confirmDelivery 方法 - 已经确认送达

    @Test
    @DisplayName("测试确认送达 - 订单已送达")
    public void testConfirmDelivery_OrderAlreadyDelivered() {
        // 准备数据
        Long deliveryManId = 1L;
        Long orderId = 1L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.DELIVERED); // 已经送达
        order.setDeliveryMan(deliveryMan);
        order.setOrderTime(new Date());

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.confirmDelivery(deliveryManId, orderId);
        });

        assertEquals("订单当前状态无法确认送达", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    // 6. 测试 confirmPickup 方法 - 已经被其他外卖员接单

    @Test
    @DisplayName("测试确认取餐 - 订单已被其他外卖员接单")
    public void testConfirmPickup_OrderAlreadyAssigned() {
        // 准备数据
        Long deliveryManId = 1L;
        Long orderId = 1L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        DeliveryMan otherDeliveryMan = new DeliveryMan();
        otherDeliveryMan.setId(2L);
        otherDeliveryMan.setUsername("delivery2");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.REQUESTING_DELIVERY);
        order.setDeliveryMan(otherDeliveryMan); // 已被其他外卖员接单
        order.setOrderTime(new Date());

        // 模拟仓库行为
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryManService.confirmPickup(deliveryManId, orderId);
        });

        assertEquals("订单已被其他外卖员接单", exception.getMessage());

        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

}
