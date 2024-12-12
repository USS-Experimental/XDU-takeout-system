package com.github.ussexperimental.takeoutsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ussexperimental.takeoutsystem.dto.OrderCreateDTO;
import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.dto.ReviewDTO;
import com.github.ussexperimental.takeoutsystem.entity.*;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import com.github.ussexperimental.takeoutsystem.entity.enums.RoleType;
import com.github.ussexperimental.takeoutsystem.entity.enums.UserType;
import com.github.ussexperimental.takeoutsystem.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
public class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
        objectMapper = new ObjectMapper();
    }

    // 1. 测试查看菜单成功
    @Test
    @DisplayName("GET /customers/menu - 查看菜单成功")
    public void testViewMenu_Success() throws Exception {
        // 准备数据
        Long merchantId = 1L;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setMerchantName("Merchant 1"); // 修改为 merchantName

        Dish dish1 = new Dish();
        dish1.setId(1L);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("10.50"));
        dish1.setDescription("Delicious dish 1");
        dish1.setImageUrl("http://example.com/dish1.jpg");
        dish1.setMerchant(merchant);

        Dish dish2 = new Dish();
        dish2.setId(2L);
        dish2.setName("Dish 2");
        dish2.setPrice(new BigDecimal("15.00"));
        dish2.setDescription("Delicious dish 2");
        dish2.setImageUrl("http://example.com/dish2.jpg");
        dish2.setMerchant(merchant);

        PageResponse<Dish> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(dish1, dish2));
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);
        pageResponse.setLast(true);

        // Mock CustomerService 的 viewMenu 方法
        when(customerService.viewMenu(eq(merchantId), anyInt(), anyInt())).thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/customers/menu")
                        .param("merchantId", merchantId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证分页信息
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                // 验证第一个菜品
                .andExpect(jsonPath("$.content[0].id").value(dish1.getId()))
                .andExpect(jsonPath("$.content[0].name").value(dish1.getName()))
                .andExpect(jsonPath("$.content[0].price").value(dish1.getPrice().doubleValue()))
                .andExpect(jsonPath("$.content[0].description").value(dish1.getDescription()))
                .andExpect(jsonPath("$.content[0].imageUrl").value(dish1.getImageUrl()))
                .andExpect(jsonPath("$.content[0].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.content[0].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                // 验证第二个菜品
                .andExpect(jsonPath("$.content[1].id").value(dish2.getId()))
                .andExpect(jsonPath("$.content[1].name").value(dish2.getName()))
                .andExpect(jsonPath("$.content[1].price").value(dish2.getPrice().doubleValue()))
                .andExpect(jsonPath("$.content[1].description").value(dish2.getDescription()))
                .andExpect(jsonPath("$.content[1].imageUrl").value(dish2.getImageUrl()))
                .andExpect(jsonPath("$.content[1].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.content[1].merchant.merchantName").value(merchant.getMerchantName())); // 修改为 merchantName

        // 验证 CustomerService.viewMenu 方法被调用一次
        verify(customerService, times(1)).viewMenu(eq(merchantId), eq(0), eq(10));
    }

    // 2. 测试查看菜单无内容
    @Test
    @DisplayName("GET /customers/menu - 查看菜单无内容")
    public void testViewMenu_NoContent() throws Exception {
        // 准备数据
        Long merchantId = 1L;

        PageResponse<Dish> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList());
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(0);
        pageResponse.setTotalPages(0);
        pageResponse.setLast(true);

        // Mock CustomerService 的 viewMenu 方法
        when(customerService.viewMenu(eq(merchantId), anyInt(), anyInt())).thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/customers/menu")
                        .param("merchantId", merchantId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 验证 CustomerService.viewMenu 方法被调用一次
        verify(customerService, times(1)).viewMenu(eq(merchantId), eq(0), eq(10));
    }

    // 3. 测试查看菜单失败（非法参数）
    @Test
    @DisplayName("GET /customers/menu - 查看菜单失败（非法参数）")
    public void testViewMenu_BadRequest() throws Exception {
        // 准备数据
        Long merchantId = -1L; // 假设非法参数

        // Mock CustomerService 的 viewMenu 方法抛出 IllegalArgumentException
        when(customerService.viewMenu(eq(merchantId), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("非法参数"));

        // 执行请求并验证响应
        mockMvc.perform(get("/customers/menu")
                        .param("merchantId", merchantId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // 验证 CustomerService.viewMenu 方法被调用一次
        verify(customerService, times(1)).viewMenu(eq(merchantId), eq(0), eq(10));
    }

    // 4. 测试创建订单成功
    @Test
    @DisplayName("POST /customers/orders - 创建订单成功")
    public void testCreateOrder_Success() throws Exception {
        // 准备数据
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setCustomerId(1L);
        orderCreateDTO.setMerchantId(2L);
        orderCreateDTO.setDishIds(Arrays.asList(1L, 2L));
        orderCreateDTO.setDeliveryTime(new Date());
        orderCreateDTO.setDeliveryLocation("123 Delivery Street");

        Merchant merchant = new Merchant();
        merchant.setId(2L);
        merchant.setMerchantName("Merchant 1"); // 修改为 merchantName

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUsername("customer1");

        Dish dish1 = new Dish();
        dish1.setId(1L);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("10.50"));
        dish1.setDescription("Delicious dish 1");
        dish1.setImageUrl("http://example.com/dish1.jpg");
        dish1.setMerchant(merchant);

        Dish dish2 = new Dish();
        dish2.setId(2L);
        dish2.setName("Dish 2");
        dish2.setPrice(new BigDecimal("15.00"));
        dish2.setDescription("Delicious dish 2");
        dish2.setImageUrl("http://example.com/dish2.jpg");
        dish2.setMerchant(merchant);

        Order createdOrder = new Order();
        createdOrder.setId(1L);
        createdOrder.setCustomer(customer);
        createdOrder.setMerchant(merchant);
        createdOrder.setDishes(Arrays.asList(dish1, dish2));
        createdOrder.setTotalPrice(new BigDecimal("25.50"));
        createdOrder.setDeliveryLocation(orderCreateDTO.getDeliveryLocation());
        createdOrder.setOrderTime(new Date());
        createdOrder.setDeliveryTime(orderCreateDTO.getDeliveryTime());
        createdOrder.setStatus(OrderStatus.PENDING);

        // Mock CustomerService 的 createOrder 方法
        when(customerService.createOrder(
                eq(orderCreateDTO.getCustomerId()),
                eq(orderCreateDTO.getMerchantId()),
                eq(orderCreateDTO.getDishIds()),
                eq(orderCreateDTO.getDeliveryTime()),
                eq(orderCreateDTO.getDeliveryLocation())
        )).thenReturn(createdOrder);

        // 执行请求并验证响应
        mockMvc.perform(post("/customers/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证订单信息
                .andExpect(jsonPath("$.id").value(createdOrder.getId()))
                .andExpect(jsonPath("$.customer.id").value(customer.getId()))
                .andExpect(jsonPath("$.customer.username").value(customer.getUsername()))
                .andExpect(jsonPath("$.merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                .andExpect(jsonPath("$.dishes").isArray())
                .andExpect(jsonPath("$.dishes.length()").value(2))
                // 验证第一个菜品
                .andExpect(jsonPath("$.dishes[0].id").value(dish1.getId()))
                .andExpect(jsonPath("$.dishes[0].name").value(dish1.getName()))
                .andExpect(jsonPath("$.dishes[0].price").value(dish1.getPrice().doubleValue()))
                .andExpect(jsonPath("$.dishes[0].description").value(dish1.getDescription()))
                .andExpect(jsonPath("$.dishes[0].imageUrl").value(dish1.getImageUrl()))
                .andExpect(jsonPath("$.dishes[0].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.dishes[0].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                // 验证第二个菜品
                .andExpect(jsonPath("$.dishes[1].id").value(dish2.getId()))
                .andExpect(jsonPath("$.dishes[1].name").value(dish2.getName()))
                .andExpect(jsonPath("$.dishes[1].price").value(dish2.getPrice().doubleValue()))
                .andExpect(jsonPath("$.dishes[1].description").value(dish2.getDescription()))
                .andExpect(jsonPath("$.dishes[1].imageUrl").value(dish2.getImageUrl()))
                .andExpect(jsonPath("$.dishes[1].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.dishes[1].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                .andExpect(jsonPath("$.totalPrice").value(createdOrder.getTotalPrice().doubleValue()))
                .andExpect(jsonPath("$.deliveryLocation").value(createdOrder.getDeliveryLocation()))
                .andExpect(jsonPath("$.status").value(createdOrder.getStatus().toString()));

        // 验证 CustomerService.createOrder 方法被调用一次
        verify(customerService, times(1)).createOrder(
                eq(orderCreateDTO.getCustomerId()),
                eq(orderCreateDTO.getMerchantId()),
                eq(orderCreateDTO.getDishIds()),
                eq(orderCreateDTO.getDeliveryTime()),
                eq(orderCreateDTO.getDeliveryLocation())
        );
    }

    // 5. 测试创建订单失败（非法参数）
    @Test
    @DisplayName("POST /customers/orders - 创建订单失败（非法参数）")
    public void testCreateOrder_BadRequest() throws Exception {
        // 准备数据
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setCustomerId(-1L); // 假设非法
        orderCreateDTO.setMerchantId(2L);
        orderCreateDTO.setDishIds(Arrays.asList(1L, 2L));
        orderCreateDTO.setDeliveryTime(new Date());
        orderCreateDTO.setDeliveryLocation("123 Delivery Street");

        // Mock CustomerService 的 createOrder 方法抛出 IllegalArgumentException
        when(customerService.createOrder(
                eq(orderCreateDTO.getCustomerId()),
                eq(orderCreateDTO.getMerchantId()),
                eq(orderCreateDTO.getDishIds()),
                eq(orderCreateDTO.getDeliveryTime()),
                eq(orderCreateDTO.getDeliveryLocation())
        )).thenThrow(new IllegalArgumentException("非法参数"));

        // 执行请求并验证响应
        mockMvc.perform(post("/customers/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderCreateDTO)))
                .andExpect(status().isBadRequest());

        // 验证 CustomerService.createOrder 方法被调用一次
        verify(customerService, times(1)).createOrder(
                eq(orderCreateDTO.getCustomerId()),
                eq(orderCreateDTO.getMerchantId()),
                eq(orderCreateDTO.getDishIds()),
                eq(orderCreateDTO.getDeliveryTime()),
                eq(orderCreateDTO.getDeliveryLocation())
        );
    }

    // 6. 测试查看顾客所有订单成功
    @Test
    @DisplayName("GET /customers/orders - 查看顾客所有订单成功")
    public void testGetMyOrders_Success() throws Exception {
        // 准备数据
        Long customerId = 1L;

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Merchant merchant = new Merchant();
        merchant.setId(2L);
        merchant.setMerchantName("Merchant 1"); // 修改为 merchantName

        Dish dish1 = new Dish();
        dish1.setId(1L);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("10.50"));
        dish1.setDescription("Delicious dish 1");
        dish1.setImageUrl("http://example.com/dish1.jpg");
        dish1.setMerchant(merchant);

        Dish dish2 = new Dish();
        dish2.setId(2L);
        dish2.setName("Dish 2");
        dish2.setPrice(new BigDecimal("15.00"));
        dish2.setDescription("Delicious dish 2");
        dish2.setImageUrl("http://example.com/dish2.jpg");
        dish2.setMerchant(merchant);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setCustomer(customer);
        order1.setMerchant(merchant);
        order1.setDishes(Arrays.asList(dish1, dish2));
        order1.setTotalPrice(new BigDecimal("25.50"));
        order1.setDeliveryLocation("123 Delivery Street");
        order1.setOrderTime(new Date());
        order1.setDeliveryTime(new Date());
        order1.setStatus(OrderStatus.PENDING);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomer(customer);
        order2.setMerchant(merchant);
        order2.setDishes(Arrays.asList(dish2));
        order2.setTotalPrice(new BigDecimal("15.00"));
        order2.setDeliveryLocation("123 Delivery Street");
        order2.setOrderTime(new Date());
        order2.setDeliveryTime(new Date());
        order2.setStatus(OrderStatus.PENDING_CONFIRMATION);

        PageResponse<Order> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(order1, order2));
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);
        pageResponse.setLast(true);

        // Mock CustomerService 的 getMyOrders 方法
        when(customerService.getMyOrders(eq(customerId), anyInt(), anyInt())).thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/customers/orders")
                        .param("customerId", customerId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证分页信息
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                // 验证第一个订单
                .andExpect(jsonPath("$.content[0].id").value(order1.getId()))
                .andExpect(jsonPath("$.content[0].customer.id").value(customer.getId()))
                .andExpect(jsonPath("$.content[0].customer.username").value(customer.getUsername()))
                .andExpect(jsonPath("$.content[0].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.content[0].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                .andExpect(jsonPath("$.content[0].dishes").isArray())
                .andExpect(jsonPath("$.content[0].dishes.length()").value(2))
                // 验证第一个订单的第一个菜品
                .andExpect(jsonPath("$.content[0].dishes[0].id").value(dish1.getId()))
                .andExpect(jsonPath("$.content[0].dishes[0].name").value(dish1.getName()))
                .andExpect(jsonPath("$.content[0].dishes[0].price").value(dish1.getPrice().doubleValue()))
                .andExpect(jsonPath("$.content[0].dishes[0].description").value(dish1.getDescription()))
                .andExpect(jsonPath("$.content[0].dishes[0].imageUrl").value(dish1.getImageUrl()))
                .andExpect(jsonPath("$.content[0].dishes[0].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.content[0].dishes[0].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                // 验证第一个订单的第二个菜品
                .andExpect(jsonPath("$.content[0].dishes[1].id").value(dish2.getId()))
                .andExpect(jsonPath("$.content[0].dishes[1].name").value(dish2.getName()))
                .andExpect(jsonPath("$.content[0].dishes[1].price").value(dish2.getPrice().doubleValue()))
                .andExpect(jsonPath("$.content[0].dishes[1].description").value(dish2.getDescription()))
                .andExpect(jsonPath("$.content[0].dishes[1].imageUrl").value(dish2.getImageUrl()))
                .andExpect(jsonPath("$.content[0].dishes[1].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.content[0].dishes[1].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                .andExpect(jsonPath("$.content[0].totalPrice").value(order1.getTotalPrice().doubleValue()))
                .andExpect(jsonPath("$.content[0].deliveryLocation").value(order1.getDeliveryLocation()))
                .andExpect(jsonPath("$.content[0].status").value(order1.getStatus().toString()))
                // 验证第二个订单
                .andExpect(jsonPath("$.content[1].id").value(order2.getId()))
                .andExpect(jsonPath("$.content[1].customer.id").value(customer.getId()))
                .andExpect(jsonPath("$.content[1].customer.username").value(customer.getUsername()))
                .andExpect(jsonPath("$.content[1].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.content[1].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                .andExpect(jsonPath("$.content[1].dishes").isArray())
                .andExpect(jsonPath("$.content[1].dishes.length()").value(1))
                // 验证第二个订单的第一个菜品
                .andExpect(jsonPath("$.content[1].dishes[0].id").value(dish2.getId()))
                .andExpect(jsonPath("$.content[1].dishes[0].name").value(dish2.getName()))
                .andExpect(jsonPath("$.content[1].dishes[0].price").value(dish2.getPrice().doubleValue()))
                .andExpect(jsonPath("$.content[1].dishes[0].description").value(dish2.getDescription()))
                .andExpect(jsonPath("$.content[1].dishes[0].imageUrl").value(dish2.getImageUrl()))
                .andExpect(jsonPath("$.content[1].dishes[0].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.content[1].dishes[0].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                .andExpect(jsonPath("$.content[1].totalPrice").value(order2.getTotalPrice().doubleValue()))
                .andExpect(jsonPath("$.content[1].deliveryLocation").value(order2.getDeliveryLocation()))
                .andExpect(jsonPath("$.content[1].status").value(order2.getStatus().toString()));

        // 验证 CustomerService.getMyOrders 方法被调用一次
        verify(customerService, times(1)).getMyOrders(eq(customerId), eq(0), eq(10));
    }

    // 7. 测试查看顾客所有订单无内容
    @Test
    @DisplayName("GET /customers/orders - 查看顾客所有订单无内容")
    public void testGetMyOrders_NoContent() throws Exception {
        // 准备数据
        Long customerId = 1L;

        PageResponse<Order> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList());
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(0);
        pageResponse.setTotalPages(0);
        pageResponse.setLast(true);

        // Mock CustomerService 的 getMyOrders 方法
        when(customerService.getMyOrders(eq(customerId), anyInt(), anyInt())).thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/customers/orders")
                        .param("customerId", customerId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 验证 CustomerService.getMyOrders 方法被调用一次
        verify(customerService, times(1)).getMyOrders(eq(customerId), eq(0), eq(10));
    }

    // 9. 测试查看单个订单详情成功
    @Test
    @DisplayName("GET /customers/orders/{orderId} - 查看单个订单详情成功")
    public void testGetOrderDetails_Success() throws Exception {
        // 准备数据
        Long orderId = 1L;
        Long customerId = 1L;

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("customer1");

        Merchant merchant = new Merchant();
        merchant.setId(2L);
        merchant.setMerchantName("Merchant 1"); // 修改为 merchantName

        Dish dish1 = new Dish();
        dish1.setId(1L);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("10.50"));
        dish1.setDescription("Delicious dish 1");
        dish1.setImageUrl("http://example.com/dish1.jpg");
        dish1.setMerchant(merchant);

        Dish dish2 = new Dish();
        dish2.setId(2L);
        dish2.setName("Dish 2");
        dish2.setPrice(new BigDecimal("15.00"));
        dish2.setDescription("Delicious dish 2");
        dish2.setImageUrl("http://example.com/dish2.jpg");
        dish2.setMerchant(merchant);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setMerchant(merchant);
        order.setDishes(Arrays.asList(dish1, dish2));
        order.setTotalPrice(new BigDecimal("25.50"));
        order.setDeliveryLocation("123 Delivery Street");
        order.setOrderTime(new Date());
        order.setDeliveryTime(new Date());
        order.setStatus(OrderStatus.PENDING);

        // Mock CustomerService 的 getOrderDetails 方法
        when(customerService.getOrderDetails(eq(customerId), eq(orderId))).thenReturn(order);

        // 执行请求并验证响应
        mockMvc.perform(get("/customers/orders/{orderId}", orderId)
                        .param("customerId", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证订单信息
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.customer.id").value(customer.getId()))
                .andExpect(jsonPath("$.customer.username").value(customer.getUsername()))
                .andExpect(jsonPath("$.merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                .andExpect(jsonPath("$.dishes").isArray())
                .andExpect(jsonPath("$.dishes.length()").value(2))
                // 验证第一个菜品
                .andExpect(jsonPath("$.dishes[0].id").value(dish1.getId()))
                .andExpect(jsonPath("$.dishes[0].name").value(dish1.getName()))
                .andExpect(jsonPath("$.dishes[0].price").value(dish1.getPrice().doubleValue()))
                .andExpect(jsonPath("$.dishes[0].description").value(dish1.getDescription()))
                .andExpect(jsonPath("$.dishes[0].imageUrl").value(dish1.getImageUrl()))
                .andExpect(jsonPath("$.dishes[0].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.dishes[0].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                // 验证第二个菜品
                .andExpect(jsonPath("$.dishes[1].id").value(dish2.getId()))
                .andExpect(jsonPath("$.dishes[1].name").value(dish2.getName()))
                .andExpect(jsonPath("$.dishes[1].price").value(dish2.getPrice().doubleValue()))
                .andExpect(jsonPath("$.dishes[1].description").value(dish2.getDescription()))
                .andExpect(jsonPath("$.dishes[1].imageUrl").value(dish2.getImageUrl()))
                .andExpect(jsonPath("$.dishes[1].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.dishes[1].merchant.merchantName").value(merchant.getMerchantName())) // 修改为 merchantName
                .andExpect(jsonPath("$.totalPrice").value(order.getTotalPrice().doubleValue()))
                .andExpect(jsonPath("$.deliveryLocation").value(order.getDeliveryLocation()))
                .andExpect(jsonPath("$.status").value(order.getStatus().toString()));

        // 验证 CustomerService.getOrderDetails 方法被调用一次
        verify(customerService, times(1)).getOrderDetails(eq(customerId), eq(orderId));
    }

    // 10. 测试查看单个订单详情失败（未找到）
    @Test
    @DisplayName("GET /customers/orders/{orderId} - 查看单个订单详情失败（未找到）")
    public void testGetOrderDetails_NotFound() throws Exception {
        // 准备数据
        Long orderId = 100L;
        Long customerId = 1L;

        // Mock CustomerService 的 getOrderDetails 方法抛出 IllegalArgumentException
        when(customerService.getOrderDetails(eq(customerId), eq(orderId)))
                .thenThrow(new IllegalArgumentException("订单未找到"));

        // 执行请求并验证响应
        mockMvc.perform(get("/customers/orders/{orderId}", orderId)
                        .param("customerId", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // 验证 CustomerService.getOrderDetails 方法被调用一次
        verify(customerService, times(1)).getOrderDetails(eq(customerId), eq(orderId));
    }

    // 11. 测试评价订单成功
    @Test
    @DisplayName("POST /customers/orders/{orderId}/reviews - 评价订单成功")
    public void testReviewOrder_Success() throws Exception {
        // 准备数据
        Long orderId = 1L;
        Long customerId = 1L;

        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setRating(5);
        reviewDTO.setComment("Excellent service!");

        Review review = new Review();
        review.setId(1L);
        // Assume Order is already set in Review
        Order order = new Order();
        order.setId(orderId);
        review.setOrder(order);
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());

        // Mock CustomerService 的 reviewOrder 方法
        when(customerService.reviewOrder(
                eq(customerId),
                eq(orderId),
                eq(reviewDTO.getRating()),
                eq(reviewDTO.getComment())
        )).thenReturn(review);

        // 执行请求并验证响应
        mockMvc.perform(post("/customers/orders/{orderId}/reviews", orderId)
                        .param("customerId", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证评价信息
                .andExpect(jsonPath("$.id").value(review.getId()))
                .andExpect(jsonPath("$.order.id").value(order.getId()))
                .andExpect(jsonPath("$.rating").value(review.getRating()))
                .andExpect(jsonPath("$.comment").value(review.getComment()));

        // 验证 CustomerService.reviewOrder 方法被调用一次
        verify(customerService, times(1)).reviewOrder(
                eq(customerId),
                eq(orderId),
                eq(reviewDTO.getRating()),
                eq(reviewDTO.getComment())
        );
    }

    // 12. 测试评价订单失败（非法参数）
    @Test
    @DisplayName("POST /customers/orders/{orderId}/reviews - 评价订单失败（非法参数）")
    public void testReviewOrder_BadRequest() throws Exception {
        // 准备数据
        Long orderId = 1L;
        Long customerId = 1L;

        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setRating(6); // 假设非法评分，评分范围1-5
        reviewDTO.setComment("Too good!");

        // Mock CustomerService 的 reviewOrder 方法抛出 IllegalArgumentException
        when(customerService.reviewOrder(
                eq(customerId),
                eq(orderId),
                eq(reviewDTO.getRating()),
                eq(reviewDTO.getComment())
        )).thenThrow(new IllegalArgumentException("非法评分"));

        // 执行请求并验证响应
        mockMvc.perform(post("/customers/orders/{orderId}/reviews", orderId)
                        .param("customerId", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isBadRequest());

        // 验证 CustomerService.reviewOrder 方法被调用一次
        verify(customerService, times(1)).reviewOrder(
                eq(customerId),
                eq(orderId),
                eq(reviewDTO.getRating()),
                eq(reviewDTO.getComment())
        );
    }
}
