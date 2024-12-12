package com.github.ussexperimental.takeoutsystem.service;

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
import com.github.ussexperimental.takeoutsystem.service.impl.MerchantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MerchantServiceImplTest {

    @InjectMocks
    private MerchantServiceImpl merchantService;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryManRepository deliveryManRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 1. 测试 addDish 方法

    @Test
    @DisplayName("测试添加菜品 - 成功")
    public void testAddDish_Success() {
        // 准备数据
        Long merchantId = 1L;
        String name = "宫保鸡丁";
        BigDecimal price = new BigDecimal("25.50");
        String description = "经典川菜";
        String imageUrl = "http://example.com/dish.jpg";

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        Dish dish = new Dish();
        dish.setId(1L);
        dish.setMerchant(merchant);
        dish.setName(name);
        dish.setPrice(price);
        dish.setDescription(description);
        dish.setImageUrl(imageUrl);

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> {
            Dish d = invocation.getArgument(0);
            d.setId(1L);
            return d;
        });

        // 调用方法
        Dish createdDish = merchantService.addDish(merchantId, name, price, description, imageUrl);

        // 验证
        assertNotNull(createdDish);
        assertEquals(1L, createdDish.getId());
        assertEquals(name, createdDish.getName());
        assertEquals(price, createdDish.getPrice());
        assertEquals(description, createdDish.getDescription());
        assertEquals(imageUrl, createdDish.getImageUrl());
        assertEquals(merchant, createdDish.getMerchant());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(dishRepository, times(1)).save(any(Dish.class));
    }

    @Test
    @DisplayName("测试添加菜品 - 商家不存在")
    public void testAddDish_MerchantNotFound() {
        // 准备数据
        Long merchantId = 100L;
        String name = "宫保鸡丁";
        BigDecimal price = new BigDecimal("25.50");
        String description = "经典川菜";
        String imageUrl = "http://example.com/dish.jpg";

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.addDish(merchantId, name, price, description, imageUrl);
        });

        assertEquals("商家不存在", exception.getMessage());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(dishRepository, never()).save(any(Dish.class));
    }

    // 2. 测试 getMenu 方法

    @Test
    @DisplayName("测试获取菜单 - 成功")
    public void testGetMenu_Success() {
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
        PageResponse<Dish> response = merchantService.getMenu(merchantId, page, size);

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
    @DisplayName("测试获取菜单 - 商家不存在")
    public void testGetMenu_MerchantNotFound() {
        // 准备数据
        Long merchantId = 100L;
        int page = 0;
        int size = 10;

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.getMenu(merchantId, page, size);
        });

        assertEquals("商家不存在", exception.getMessage());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(dishRepository, never()).findByMerchant(any(Merchant.class), any(Pageable.class));
    }

    // 3. 测试 updateDish 方法

    @Test
    @DisplayName("测试更新菜品 - 成功")
    public void testUpdateDish_Success() {
        // 准备数据
        Long dishId = 1L;
        String newName = "鱼香肉丝";
        BigDecimal newPrice = new BigDecimal("22.00");
        String newDescription = "新描述";
        String newImageUrl = "http://example.com/new_dish.jpg";

        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setUsername("merchant1");

        Dish existingDish = new Dish();
        existingDish.setId(dishId);
        existingDish.setMerchant(merchant);
        existingDish.setName("宫保鸡丁");
        existingDish.setPrice(new BigDecimal("25.50"));
        existingDish.setDescription("经典川菜");
        existingDish.setImageUrl("http://example.com/dish.jpg");

        Dish updatedDish = new Dish();
        updatedDish.setId(dishId);
        updatedDish.setMerchant(merchant);
        updatedDish.setName(newName);
        updatedDish.setPrice(newPrice);
        updatedDish.setDescription(newDescription);
        updatedDish.setImageUrl(newImageUrl);

        // 模拟仓库行为
        when(dishRepository.findById(dishId)).thenReturn(Optional.of(existingDish));
        when(dishRepository.save(any(Dish.class))).thenReturn(updatedDish);

        // 调用方法
        Dish result = merchantService.updateDish(dishId, newName, newPrice, newDescription, newImageUrl);

        // 验证
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(newPrice, result.getPrice());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newImageUrl, result.getImageUrl());

        verify(dishRepository, times(1)).findById(dishId);
        verify(dishRepository, times(1)).save(existingDish);
    }

    @Test
    @DisplayName("测试更新菜品 - 菜品不存在")
    public void testUpdateDish_DishNotFound() {
        // 准备数据
        Long dishId = 100L;
        String newName = "鱼香肉丝";
        BigDecimal newPrice = new BigDecimal("22.00");
        String newDescription = "新描述";
        String newImageUrl = "http://example.com/new_dish.jpg";

        // 模拟仓库行为
        when(dishRepository.findById(dishId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.updateDish(dishId, newName, newPrice, newDescription, newImageUrl);
        });

        assertEquals("菜品不存在", exception.getMessage());

        verify(dishRepository, times(1)).findById(dishId);
        verify(dishRepository, never()).save(any(Dish.class));
    }

    // 4. 测试 deleteDish 方法

    @Test
    @DisplayName("测试删除菜品 - 成功")
    public void testDeleteDish_Success() {
        // 准备数据
        Long dishId = 1L;

        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setUsername("merchant1");

        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setMerchant(merchant);

        // 模拟仓库行为
        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));
        doNothing().when(dishRepository).delete(dish);

        // 调用方法
        assertDoesNotThrow(() -> merchantService.deleteDish(dishId));

        // 验证
        verify(dishRepository, times(1)).findById(dishId);
        verify(dishRepository, times(1)).delete(dish);
    }

    @Test
    @DisplayName("测试删除菜品 - 菜品不存在")
    public void testDeleteDish_DishNotFound() {
        // 准备数据
        Long dishId = 100L;

        // 模拟仓库行为
        when(dishRepository.findById(dishId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.deleteDish(dishId);
        });

        assertEquals("菜品不存在", exception.getMessage());

        verify(dishRepository, times(1)).findById(dishId);
        verify(dishRepository, never()).delete(any(Dish.class));
    }

    // 5. 测试 viewSales 方法

    @Test
    @DisplayName("测试查看销售记录 - 成功（有日期范围）")
    public void testViewSales_WithDateRange_Success() {
        // 准备数据
        Long merchantId = 1L;
        Date startDate = new GregorianCalendar(2024, Calendar.JANUARY, 1).getTime();
        Date endDate = new GregorianCalendar(2024, Calendar.DECEMBER, 31).getTime();
        int page = 0;
        int size = 10;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setMerchant(merchant);
        order1.setOrderTime(new GregorianCalendar(2024, Calendar.JUNE, 15).getTime());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setMerchant(merchant);
        order2.setOrderTime(new GregorianCalendar(2024, Calendar.NOVEMBER, 20).getTime());

        List<Order> orders = Arrays.asList(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(page, size), orders.size());

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(orderRepository.findByMerchantAndOrderTimeBetween(merchant, startDate, endDate, PageRequest.of(page, size, Sort.by("orderTime").descending())))
                .thenReturn(orderPage);

        // 调用方法
        PageResponse<Order> response = merchantService.viewSales(merchantId, startDate, endDate, page, size);

        // 验证
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, times(1)).findByMerchantAndOrderTimeBetween(merchant, startDate, endDate, PageRequest.of(page, size, Sort.by("orderTime").descending()));
    }

    @Test
    @DisplayName("测试查看销售记录 - 成功（无日期范围）")
    public void testViewSales_WithoutDateRange_Success() {
        // 准备数据
        Long merchantId = 1L;
        Date startDate = null;
        Date endDate = null;
        int page = 0;
        int size = 10;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setMerchant(merchant);
        order1.setOrderTime(new GregorianCalendar(2024, Calendar.JUNE, 15).getTime());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setMerchant(merchant);
        order2.setOrderTime(new GregorianCalendar(2024, Calendar.NOVEMBER, 20).getTime());

        List<Order> orders = Arrays.asList(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(page, size), orders.size());

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(orderRepository.findByMerchant(merchant, PageRequest.of(page, size, Sort.by("orderTime").descending())))
                .thenReturn(orderPage);

        // 调用方法
        PageResponse<Order> response = merchantService.viewSales(merchantId, startDate, endDate, page, size);

        // 验证
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, times(1)).findByMerchant(merchant, PageRequest.of(page, size, Sort.by("orderTime").descending()));
    }

    @Test
    @DisplayName("测试查看销售记录 - 商家不存在")
    public void testViewSales_MerchantNotFound() {
        // 准备数据
        Long merchantId = 100L;
        Date startDate = new Date();
        Date endDate = new Date();
        int page = 0;
        int size = 10;

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.viewSales(merchantId, startDate, endDate, page, size);
        });

        assertEquals("商家不存在", exception.getMessage());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, never()).findByMerchantAndOrderTimeBetween(any(Merchant.class), any(Date.class), any(Date.class), any(Pageable.class));
        verify(orderRepository, never()).findByMerchant(any(Merchant.class), any(Pageable.class));
    }

    // 6. 测试 viewPendingOrders 方法

    @Test
    @DisplayName("测试查看待处理订单 - 成功")
    public void testViewPendingOrders_Success() {
        // 准备数据
        Long merchantId = 1L;
        int page = 0;
        int size = 10;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setMerchant(merchant);
        order1.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order1.setOrderTime(new Date());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setMerchant(merchant);
        order2.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order2.setOrderTime(new Date());

        List<Order> pendingOrders = Arrays.asList(order1, order2);
        Page<Order> orderPage = new PageImpl<>(pendingOrders, PageRequest.of(page, size), pendingOrders.size());

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(orderRepository.findByMerchantAndStatus(merchant, OrderStatus.PENDING_CONFIRMATION, PageRequest.of(page, size, Sort.by("orderTime").descending())))
                .thenReturn(orderPage);

        // 调用方法
        PageResponse<Order> response = merchantService.viewPendingOrders(merchantId, page, size);

        // 验证
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, times(1)).findByMerchantAndStatus(merchant, OrderStatus.PENDING_CONFIRMATION, PageRequest.of(page, size, Sort.by("orderTime").descending()));
    }

    @Test
    @DisplayName("测试查看待处理订单 - 商家不存在")
    public void testViewPendingOrders_MerchantNotFound() {
        // 准备数据
        Long merchantId = 100L;
        int page = 0;
        int size = 10;

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.viewPendingOrders(merchantId, page, size);
        });

        assertEquals("商家不存在", exception.getMessage());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, never()).findByMerchantAndStatus(any(Merchant.class), any(OrderStatus.class), any(Pageable.class));
    }

    // 7. 测试 acceptOrder 方法

    @Test
    @DisplayName("测试接受订单 - 成功")
    public void testAcceptOrder_Success() {
        // 准备数据
        Long merchantId = 1L;
        Long orderId = 1L;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        Order order = new Order();
        order.setId(orderId);
        order.setMerchant(merchant);
        order.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order.setOrderTime(new Date());

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setMerchant(merchant);
        updatedOrder.setStatus(OrderStatus.PREPARING);
        updatedOrder.setOrderTime(order.getOrderTime());

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(updatedOrder);

        // 调用方法
        Order result = merchantService.acceptOrder(merchantId, orderId);

        // 验证
        assertNotNull(result);
        assertEquals(OrderStatus.PREPARING, result.getStatus());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("测试接受订单 - 商家不存在")
    public void testAcceptOrder_MerchantNotFound() {
        // 准备数据
        Long merchantId = 100L;
        Long orderId = 1L;

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.acceptOrder(merchantId, orderId);
        });

        assertEquals("商家不存在", exception.getMessage());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试接受订单 - 订单不存在")
    public void testAcceptOrder_OrderNotFound() {
        // 准备数据
        Long merchantId = 1L;
        Long orderId = 100L;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.acceptOrder(merchantId, orderId);
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试接受订单 - 无权接受该订单")
    public void testAcceptOrder_UnauthorizedAccess() {
        // 准备数据
        Long merchantId = 1L;
        Long orderId = 1L;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        Merchant otherMerchant = new Merchant();
        otherMerchant.setId(2L);
        otherMerchant.setUsername("merchant2");

        Order order = new Order();
        order.setId(orderId);
        order.setMerchant(otherMerchant);
        order.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order.setOrderTime(new Date());

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.acceptOrder(merchantId, orderId);
        });

        assertEquals("无权接受该订单", exception.getMessage());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试接受订单 - 订单状态不正确")
    public void testAcceptOrder_InvalidOrderStatus() {
        // 准备数据
        Long merchantId = 1L;
        Long orderId = 1L;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setUsername("merchant1");

        Order order = new Order();
        order.setId(orderId);
        order.setMerchant(merchant);
        order.setStatus(OrderStatus.PREPARING); // 不是 PENDING_CONFIRMATION
        order.setOrderTime(new Date());

        // 模拟仓库行为
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.acceptOrder(merchantId, orderId);
        });

        assertEquals("订单当前状态无法接受", exception.getMessage());

        verify(merchantRepository, times(1)).findById(merchantId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    // 8. 测试 requestDelivery 方法

    @Test
    @DisplayName("测试请求送餐 - 成功")
    public void testRequestDelivery_Success() {
        // 准备数据
        Long orderId = 1L;
        Long deliveryManId = 1L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PREPARING);
        order.setDeliveryMan(null);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.DELIVERING);
        updatedOrder.setDeliveryMan(deliveryMan);

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.save(order)).thenReturn(updatedOrder);

        // 调用方法
        Order result = merchantService.requestDelivery(orderId, deliveryManId);

        // 验证
        assertNotNull(result);
        assertEquals(OrderStatus.DELIVERING, result.getStatus());
        assertEquals(deliveryMan, result.getDeliveryMan());

        verify(orderRepository, times(1)).findById(orderId);
        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("测试请求送餐 - 订单不存在")
    public void testRequestDelivery_OrderNotFound() {
        // 准备数据
        Long orderId = 100L;
        Long deliveryManId = 1L;

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.requestDelivery(orderId, deliveryManId);
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verify(deliveryManRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试请求送餐 - 送餐员不存在")
    public void testRequestDelivery_DeliveryManNotFound() {
        // 准备数据
        Long orderId = 1L;
        Long deliveryManId = 100L;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PREPARING);
        order.setDeliveryMan(null);

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(deliveryManRepository.findById(deliveryManId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.requestDelivery(orderId, deliveryManId);
        });

        assertEquals("送餐员不存在", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verify(deliveryManRepository, times(1)).findById(deliveryManId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试请求送餐 - 订单状态不正确")
    public void testRequestDelivery_InvalidOrderStatus() {
        // 准备数据
        Long orderId = 1L;
        Long deliveryManId = 1L;

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(deliveryManId);
        deliveryMan.setUsername("delivery1");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.DELIVERING); // 不是 PREPARING
        order.setDeliveryMan(null);

        // 模拟仓库行为
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            merchantService.requestDelivery(orderId, deliveryManId);
        });

        assertEquals("订单当前状态无法请求送餐", exception.getMessage());

        verify(orderRepository, times(1)).findById(orderId);
        verify(deliveryManRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }
}
