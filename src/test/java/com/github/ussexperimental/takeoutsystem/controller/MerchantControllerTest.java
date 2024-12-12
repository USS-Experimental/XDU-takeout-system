package com.github.ussexperimental.takeoutsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ussexperimental.takeoutsystem.dto.DishCreateDTO;
import com.github.ussexperimental.takeoutsystem.dto.DishUpdateDTO;
import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.dto.RequestDeliveryDTO;
import com.github.ussexperimental.takeoutsystem.entity.DeliveryMan;
import com.github.ussexperimental.takeoutsystem.entity.Dish;
import com.github.ussexperimental.takeoutsystem.entity.Merchant;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.entity.enums.OrderStatus;
import com.github.ussexperimental.takeoutsystem.service.ImageService;
import com.github.ussexperimental.takeoutsystem.service.MerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
public class MerchantControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MerchantService merchantService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private MerchantController merchantController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(merchantController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * 1. 测试创建菜品成功
     * POST /merchants/menu
     */
    @Test
    @DisplayName("POST /merchants/menu - 创建菜品成功")
    public void testCreateDish_Success() throws Exception {
        // 准备数据
        DishCreateDTO dishCreateDTO = new DishCreateDTO();
        dishCreateDTO.setMerchantId(1L);
        dishCreateDTO.setName("New Dish");
        dishCreateDTO.setPrice(new BigDecimal("12.99"));
        dishCreateDTO.setDescription("A delicious new dish");
        dishCreateDTO.setImageUrl("http://example.com/dish.jpg");

        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setMerchantName("Merchant 1");

        Dish createdDish = new Dish();
        createdDish.setId(1L);
        createdDish.setMerchant(merchant);
        createdDish.setName(dishCreateDTO.getName());
        createdDish.setPrice(dishCreateDTO.getPrice());
        createdDish.setDescription(dishCreateDTO.getDescription());
        createdDish.setImageUrl(dishCreateDTO.getImageUrl());

        // Mock MerchantService 的 addDish 方法
        when(merchantService.addDish(
                eq(dishCreateDTO.getMerchantId()),
                eq(dishCreateDTO.getName()),
                eq(dishCreateDTO.getPrice()),
                eq(dishCreateDTO.getDescription()),
                eq(dishCreateDTO.getImageUrl())
        )).thenReturn(createdDish);

        // 执行请求并验证响应
        mockMvc.perform(post("/merchants/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dishCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证菜品信息
                .andExpect(jsonPath("$.id").value(createdDish.getId()))
                .andExpect(jsonPath("$.name").value(createdDish.getName()))
                .andExpect(jsonPath("$.price").value(createdDish.getPrice().doubleValue()))
                .andExpect(jsonPath("$.description").value(createdDish.getDescription()))
                .andExpect(jsonPath("$.imageUrl").value(createdDish.getImageUrl()))
                .andExpect(jsonPath("$.merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.merchant.merchantName").value(merchant.getMerchantName()));

        // 验证 MerchantService.addDish 方法被调用一次
        verify(merchantService, times(1)).addDish(
                eq(dishCreateDTO.getMerchantId()),
                eq(dishCreateDTO.getName()),
                eq(dishCreateDTO.getPrice()),
                eq(dishCreateDTO.getDescription()),
                eq(dishCreateDTO.getImageUrl())
        );
    }

    /**
     * 2. 测试创建菜品失败（非法参数）
     * POST /merchants/menu
     */
    @Test
    @DisplayName("POST /merchants/menu - 创建菜品失败（非法参数）")
    public void testCreateDish_BadRequest() throws Exception {
        // 准备数据
        DishCreateDTO dishCreateDTO = new DishCreateDTO();
        dishCreateDTO.setMerchantId(-1L); // 假设非法 MerchantId
        dishCreateDTO.setName(""); // 假设非法名称
        dishCreateDTO.setPrice(new BigDecimal("-10.00")); // 假设非法价格
        dishCreateDTO.setDescription("Invalid dish");
        dishCreateDTO.setImageUrl("invalid_url");

        // Mock MerchantService 的 addDish 方法抛出 IllegalArgumentException
        when(merchantService.addDish(
                eq(dishCreateDTO.getMerchantId()),
                eq(dishCreateDTO.getName()),
                eq(dishCreateDTO.getPrice()),
                eq(dishCreateDTO.getDescription()),
                eq(dishCreateDTO.getImageUrl())
        )).thenThrow(new IllegalArgumentException("非法参数"));

        // 执行请求并验证响应
        mockMvc.perform(post("/merchants/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dishCreateDTO)))
                .andExpect(status().isBadRequest());

        // 验证 MerchantService.addDish 方法被调用一次
        verify(merchantService, times(1)).addDish(
                eq(dishCreateDTO.getMerchantId()),
                eq(dishCreateDTO.getName()),
                eq(dishCreateDTO.getPrice()),
                eq(dishCreateDTO.getDescription()),
                eq(dishCreateDTO.getImageUrl())
        );
    }

    /**
     * 3. 测试获取菜单成功
     * GET /merchants/menu
     */
    @Test
    @DisplayName("GET /merchants/menu - 获取菜单成功")
    public void testGetMenu_Success() throws Exception {
        // 准备数据
        Long merchantId = 1L;
        int page = 0;
        int size = 10;

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setMerchantName("Merchant 1");

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
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);
        pageResponse.setLast(true);

        // Mock MerchantService 的 getMenu 方法
        when(merchantService.getMenu(eq(merchantId), eq(page), eq(size))).thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/merchants/menu")
                        .param("merchantId", merchantId.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证分页信息
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
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
                .andExpect(jsonPath("$.content[0].merchant.merchantName").value(merchant.getMerchantName()))
                // 验证第二个菜品
                .andExpect(jsonPath("$.content[1].id").value(dish2.getId()))
                .andExpect(jsonPath("$.content[1].name").value(dish2.getName()))
                .andExpect(jsonPath("$.content[1].price").value(dish2.getPrice().doubleValue()))
                .andExpect(jsonPath("$.content[1].description").value(dish2.getDescription()))
                .andExpect(jsonPath("$.content[1].imageUrl").value(dish2.getImageUrl()))
                .andExpect(jsonPath("$.content[1].merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.content[1].merchant.merchantName").value(merchant.getMerchantName()));

        // 验证 MerchantService.getMenu 方法被调用一次
        verify(merchantService, times(1)).getMenu(eq(merchantId), eq(page), eq(size));
    }

    /**
     * 4. 测试获取菜单无内容
     * GET /merchants/menu
     */
    @Test
    @DisplayName("GET /merchants/menu - 获取菜单无内容")
    public void testGetMenu_NoContent() throws Exception {
        // 准备数据
        Long merchantId = 1L;
        int page = 0;
        int size = 10;

        PageResponse<Dish> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList());
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(0);
        pageResponse.setTotalPages(0);
        pageResponse.setLast(true);

        // Mock MerchantService 的 getMenu 方法
        when(merchantService.getMenu(eq(merchantId), eq(page), eq(size))).thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/merchants/menu")
                        .param("merchantId", merchantId.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 验证 MerchantService.getMenu 方法被调用一次
        verify(merchantService, times(1)).getMenu(eq(merchantId), eq(page), eq(size));
    }

    /**
     * 5. 测试获取菜单失败（非法参数）
     * GET /merchants/menu
     */
    @Test
    @DisplayName("GET /merchants/menu - 获取菜单失败（非法参数）")
    public void testGetMenu_BadRequest() throws Exception {
        // 准备数据
        Long merchantId = -1L; // 假设非法 MerchantId
        int page = 0;
        int size = 10;

        // Mock MerchantService 的 getMenu 方法抛出 IllegalArgumentException
        when(merchantService.getMenu(eq(merchantId), eq(page), eq(size)))
                .thenThrow(new IllegalArgumentException("非法参数"));

        // 执行请求并验证响应
        mockMvc.perform(get("/merchants/menu")
                        .param("merchantId", merchantId.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // 验证 MerchantService.getMenu 方法被调用一次
        verify(merchantService, times(1)).getMenu(eq(merchantId), eq(page), eq(size));
    }

    /**
     * 6. 测试更新菜品成功
     * PUT /merchants/menu/{dishId}
     */
    @Test
    @DisplayName("PUT /merchants/menu/{dishId} - 更新菜品成功")
    public void testUpdateDish_Success() throws Exception {
        // 准备数据
        Long dishId = 1L;

        DishUpdateDTO dishUpdateDTO = new DishUpdateDTO();
        dishUpdateDTO.setName("Updated Dish");
        dishUpdateDTO.setPrice(new BigDecimal("14.99"));
        dishUpdateDTO.setDescription("Updated description");
        dishUpdateDTO.setImageUrl("http://example.com/updated_dish.jpg");

        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setMerchantName("Merchant 1");

        Dish updatedDish = new Dish();
        updatedDish.setId(dishId);
        updatedDish.setMerchant(merchant);
        updatedDish.setName(dishUpdateDTO.getName());
        updatedDish.setPrice(dishUpdateDTO.getPrice());
        updatedDish.setDescription(dishUpdateDTO.getDescription());
        updatedDish.setImageUrl(dishUpdateDTO.getImageUrl());

        // Mock MerchantService 的 updateDish 方法
        when(merchantService.updateDish(
                eq(dishId),
                eq(dishUpdateDTO.getName()),
                eq(dishUpdateDTO.getPrice()),
                eq(dishUpdateDTO.getDescription()),
                eq(dishUpdateDTO.getImageUrl())
        )).thenReturn(updatedDish);

        // 执行请求并验证响应
        mockMvc.perform(put("/merchants/menu/{dishId}", dishId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dishUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证更新后的菜品信息
                .andExpect(jsonPath("$.id").value(updatedDish.getId()))
                .andExpect(jsonPath("$.name").value(updatedDish.getName()))
                .andExpect(jsonPath("$.price").value(updatedDish.getPrice().doubleValue()))
                .andExpect(jsonPath("$.description").value(updatedDish.getDescription()))
                .andExpect(jsonPath("$.imageUrl").value(updatedDish.getImageUrl()))
                .andExpect(jsonPath("$.merchant.id").value(merchant.getId()))
                .andExpect(jsonPath("$.merchant.merchantName").value(merchant.getMerchantName()));

        // 验证 MerchantService.updateDish 方法被调用一次
        verify(merchantService, times(1)).updateDish(
                eq(dishId),
                eq(dishUpdateDTO.getName()),
                eq(dishUpdateDTO.getPrice()),
                eq(dishUpdateDTO.getDescription()),
                eq(dishUpdateDTO.getImageUrl())
        );
    }

    /**
     * 7. 测试更新菜品失败（未找到）
     * PUT /merchants/menu/{dishId}
     */
    @Test
    @DisplayName("PUT /merchants/menu/{dishId} - 更新菜品失败（未找到）")
    public void testUpdateDish_NotFound() throws Exception {
        // 准备数据
        Long dishId = 100L; // 假设不存在的 DishId

        DishUpdateDTO dishUpdateDTO = new DishUpdateDTO();
        dishUpdateDTO.setName("Updated Dish");
        dishUpdateDTO.setPrice(new BigDecimal("14.99"));
        dishUpdateDTO.setDescription("Updated description");
        dishUpdateDTO.setImageUrl("http://example.com/updated_dish.jpg");

        // Mock MerchantService 的 updateDish 方法抛出 IllegalArgumentException
        when(merchantService.updateDish(
                eq(dishId),
                eq(dishUpdateDTO.getName()),
                eq(dishUpdateDTO.getPrice()),
                eq(dishUpdateDTO.getDescription()),
                eq(dishUpdateDTO.getImageUrl())
        )).thenThrow(new IllegalArgumentException("菜品未找到"));

        // 执行请求并验证响应
        mockMvc.perform(put("/merchants/menu/{dishId}", dishId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dishUpdateDTO)))
                .andExpect(status().isNotFound());

        // 验证 MerchantService.updateDish 方法被调用一次
        verify(merchantService, times(1)).updateDish(
                eq(dishId),
                eq(dishUpdateDTO.getName()),
                eq(dishUpdateDTO.getPrice()),
                eq(dishUpdateDTO.getDescription()),
                eq(dishUpdateDTO.getImageUrl())
        );
    }

    /**
     * 8. 测试删除菜品成功
     * DELETE /merchants/menu/{dishId}
     */
    @Test
    @DisplayName("DELETE /merchants/menu/{dishId} - 删除菜品成功")
    public void testDeleteDish_Success() throws Exception {
        // 准备数据
        Long dishId = 1L;

        // Mock MerchantService 的 deleteDish 方法（无返回值）
        doNothing().when(merchantService).deleteDish(eq(dishId));

        // 执行请求并验证响应
        mockMvc.perform(delete("/merchants/menu/{dishId}", dishId))
                .andExpect(status().isNoContent());

        // 验证 MerchantService.deleteDish 方法被调用一次
        verify(merchantService, times(1)).deleteDish(eq(dishId));
    }

    /**
     * 9. 测试删除菜品失败（未找到）
     * DELETE /merchants/menu/{dishId}
     */
    @Test
    @DisplayName("DELETE /merchants/menu/{dishId} - 删除菜品失败（未找到）")
    public void testDeleteDish_NotFound() throws Exception {
        // 准备数据
        Long dishId = 100L; // 假设不存在的 DishId

        // Mock MerchantService 的 deleteDish 方法抛出 IllegalArgumentException
        doThrow(new IllegalArgumentException("菜品未找到")).when(merchantService).deleteDish(eq(dishId));

        // 执行请求并验证响应
        mockMvc.perform(delete("/merchants/menu/{dishId}", dishId))
                .andExpect(status().isNotFound());

        // 验证 MerchantService.deleteDish 方法被调用一次
        verify(merchantService, times(1)).deleteDish(eq(dishId));
    }

    /**
     * 10. 测试查看销售情况成功
     * GET /merchants/sales
     */
    @Test
    @DisplayName("GET /merchants/sales - 查看销售情况成功")
    public void testViewSales_Success() throws Exception {
        // 准备数据
        Long merchantId = 1L;
        Date startDate = new Date(System.currentTimeMillis() - 86400000L); // 1天前
        Date endDate = new Date();
        int page = 0;
        int size = 10;

        Dish dish1 = new Dish();
        dish1.setId(1L);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("10.50"));
        dish1.setDescription("Delicious dish 1");
        dish1.setImageUrl("http://example.com/dish1.jpg");

        Dish dish2 = new Dish();
        dish2.setId(2L);
        dish2.setName("Dish 2");
        dish2.setPrice(new BigDecimal("15.00"));
        dish2.setDescription("Delicious dish 2");
        dish2.setImageUrl("http://example.com/dish2.jpg");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.DELIVERED); // 使用新的枚举
        order1.setTotalPrice(new BigDecimal("25.50"));
        order1.setDeliveryLocation("123 Delivery Street");
        order1.setOrderTime(new Date());
        order1.setDeliveryTime(new Date());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(OrderStatus.DELIVERED); // 使用新的枚举
        order2.setTotalPrice(new BigDecimal("15.00"));
        order2.setDeliveryLocation("123 Delivery Street");
        order2.setOrderTime(new Date());
        order2.setDeliveryTime(new Date());

        PageResponse<Order> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(order1, order2));
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);
        pageResponse.setLast(true);

        // Mock MerchantService 的 viewSales 方法
        when(merchantService.viewSales(eq(merchantId), eq(startDate), eq(endDate), eq(page), eq(size)))
                .thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/merchants/sales")
                        .param("merchantId", merchantId.toString())
                        .param("startDate", String.valueOf(startDate.getTime()))
                        .param("endDate", String.valueOf(endDate.getTime()))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证分页信息
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                // 验证第一个订单
                .andExpect(jsonPath("$.content[0].id").value(order1.getId()))
                .andExpect(jsonPath("$.content[0].status").value(order1.getStatus().toString()))
                .andExpect(jsonPath("$.content[0].totalPrice").value(order1.getTotalPrice().doubleValue()))
                .andExpect(jsonPath("$.content[0].deliveryLocation").value(order1.getDeliveryLocation()))
                .andExpect(jsonPath("$.content[0].orderTime").isNotEmpty())
                .andExpect(jsonPath("$.content[0].deliveryTime").isNotEmpty())
                // 验证第二个订单
                .andExpect(jsonPath("$.content[1].id").value(order2.getId()))
                .andExpect(jsonPath("$.content[1].status").value(order2.getStatus().toString()))
                .andExpect(jsonPath("$.content[1].totalPrice").value(order2.getTotalPrice().doubleValue()))
                .andExpect(jsonPath("$.content[1].deliveryLocation").value(order2.getDeliveryLocation()))
                .andExpect(jsonPath("$.content[1].orderTime").isNotEmpty())
                .andExpect(jsonPath("$.content[1].deliveryTime").isNotEmpty());

        // 验证 MerchantService.viewSales 方法被调用一次
        verify(merchantService, times(1)).viewSales(eq(merchantId), eq(startDate), eq(endDate), eq(page), eq(size));
    }

    /**
     * 11. 测试查看销售情况无内容
     * GET /merchants/sales
     */
    @Test
    @DisplayName("GET /merchants/sales - 查看销售情况无内容")
    public void testViewSales_NoContent() throws Exception {
        // 准备数据
        Long merchantId = 1L;
        Date startDate = new Date(System.currentTimeMillis() - 86400000L); // 1天前
        Date endDate = new Date();
        int page = 0;
        int size = 10;

        PageResponse<Order> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList());
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(0);
        pageResponse.setTotalPages(0);
        pageResponse.setLast(true);

        // Mock MerchantService 的 viewSales 方法
        when(merchantService.viewSales(eq(merchantId), eq(startDate), eq(endDate), eq(page), eq(size)))
                .thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/merchants/sales")
                        .param("merchantId", merchantId.toString())
                        .param("startDate", String.valueOf(startDate.getTime()))
                        .param("endDate", String.valueOf(endDate.getTime()))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 验证 MerchantService.viewSales 方法被调用一次
        verify(merchantService, times(1)).viewSales(eq(merchantId), eq(startDate), eq(endDate), eq(page), eq(size));
    }

    /**
     * 12. 测试查看销售情况失败（非法参数）
     * GET /merchants/sales
     */
    @Test
    @DisplayName("GET /merchants/sales - 查看销售情况失败（非法参数）")
    public void testViewSales_BadRequest() throws Exception {
        // 准备数据
        Long merchantId = -1L; // 假设非法 MerchantId
        Date startDate = new Date(System.currentTimeMillis() - 86400000L); // 1天前
        Date endDate = new Date();
        int page = 0;
        int size = 10;

        // Mock MerchantService 的 viewSales 方法抛出 IllegalArgumentException
        when(merchantService.viewSales(eq(merchantId), eq(startDate), eq(endDate), eq(page), eq(size)))
                .thenThrow(new IllegalArgumentException("非法参数"));

        // 执行请求并验证响应
        mockMvc.perform(get("/merchants/sales")
                        .param("merchantId", merchantId.toString())
                        .param("startDate", String.valueOf(startDate.getTime()))
                        .param("endDate", String.valueOf(endDate.getTime()))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // 验证 MerchantService.viewSales 方法被调用一次
        verify(merchantService, times(1)).viewSales(eq(merchantId), eq(startDate), eq(endDate), eq(page), eq(size));
    }

    /**
     * 13. 测试查看待确认订单成功
     * GET /merchants/orders/pending
     */
    @Test
    @DisplayName("GET /merchants/orders/pending - 查看待确认订单成功")
    public void testViewPendingOrders_Success() throws Exception {
        // 准备数据
        Long merchantId = 1L;
        int page = 0;
        int size = 10;

        Dish dish1 = new Dish();
        dish1.setId(1L);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("10.50"));
        dish1.setDescription("Delicious dish 1");
        dish1.setImageUrl("http://example.com/dish1.jpg");

        Order pendingOrder = new Order();
        pendingOrder.setId(1L);
        pendingOrder.setStatus(OrderStatus.PENDING_CONFIRMATION); // 使用新的枚举
        pendingOrder.setTotalPrice(new BigDecimal("25.50"));
        pendingOrder.setDeliveryLocation("123 Delivery Street");
        pendingOrder.setOrderTime(new Date());
        pendingOrder.setDeliveryTime(new Date());

        PageResponse<Order> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(pendingOrder));
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(1);
        pageResponse.setTotalPages(1);
        pageResponse.setLast(true);

        // Mock MerchantService 的 viewPendingOrders 方法
        when(merchantService.viewPendingOrders(eq(merchantId), eq(page), eq(size)))
                .thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/merchants/orders/pending")
                        .param("merchantId", merchantId.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证分页信息
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                // 验证待确认订单信息
                .andExpect(jsonPath("$.content[0].id").value(pendingOrder.getId()))
                .andExpect(jsonPath("$.content[0].status").value(pendingOrder.getStatus().toString()))
                .andExpect(jsonPath("$.content[0].totalPrice").value(pendingOrder.getTotalPrice().doubleValue()))
                .andExpect(jsonPath("$.content[0].deliveryLocation").value(pendingOrder.getDeliveryLocation()))
                .andExpect(jsonPath("$.content[0].orderTime").isNotEmpty())
                .andExpect(jsonPath("$.content[0].deliveryTime").isNotEmpty());

        // 验证 MerchantService.viewPendingOrders 方法被调用一次
        verify(merchantService, times(1)).viewPendingOrders(eq(merchantId), eq(page), eq(size));
    }

    /**
     * 14. 测试查看待确认订单无内容
     * GET /merchants/orders/pending
     */
    @Test
    @DisplayName("GET /merchants/orders/pending - 查看待确认订单无内容")
    public void testViewPendingOrders_NoContent() throws Exception {
        // 准备数据
        Long merchantId = 1L;
        int page = 0;
        int size = 10;

        PageResponse<Order> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList());
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(0);
        pageResponse.setTotalPages(0);
        pageResponse.setLast(true);

        // Mock MerchantService 的 viewPendingOrders 方法
        when(merchantService.viewPendingOrders(eq(merchantId), eq(page), eq(size)))
                .thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/merchants/orders/pending")
                        .param("merchantId", merchantId.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 验证 MerchantService.viewPendingOrders 方法被调用一次
        verify(merchantService, times(1)).viewPendingOrders(eq(merchantId), eq(page), eq(size));
    }

    /**
     * 15. 测试查看待确认订单失败（非法参数）
     * GET /merchants/orders/pending
     */
    @Test
    @DisplayName("GET /merchants/orders/pending - 查看待确认订单失败（非法参数）")
    public void testViewPendingOrders_BadRequest() throws Exception {
        // 准备数据
        Long merchantId = -1L; // 假设非法 MerchantId
        int page = 0;
        int size = 10;

        // Mock MerchantService 的 viewPendingOrders 方法抛出 IllegalArgumentException
        when(merchantService.viewPendingOrders(eq(merchantId), eq(page), eq(size)))
                .thenThrow(new IllegalArgumentException("非法参数"));

        // 执行请求并验证响应
        mockMvc.perform(get("/merchants/orders/pending")
                        .param("merchantId", merchantId.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // 验证 MerchantService.viewPendingOrders 方法被调用一次
        verify(merchantService, times(1)).viewPendingOrders(eq(merchantId), eq(page), eq(size));
    }

    /**
     * 16. 测试接受订单成功
     * PUT /merchants/orders/{orderId}/accept
     */
    @Test
    @DisplayName("PUT /merchants/orders/{orderId}/accept - 接受订单成功")
    public void testAcceptOrder_Success() throws Exception {
        // 准备数据
        Long orderId = 1L;
        Long merchantId = 1L;

        Order acceptedOrder = new Order();
        acceptedOrder.setId(orderId);
        acceptedOrder.setStatus(OrderStatus.PREPARING); // 使用新的枚举
        acceptedOrder.setTotalPrice(new BigDecimal("25.50"));
        acceptedOrder.setDeliveryLocation("123 Delivery Street");
        acceptedOrder.setOrderTime(new Date());
        acceptedOrder.setDeliveryTime(new Date());

        // Mock MerchantService 的 acceptOrder 方法
        when(merchantService.acceptOrder(eq(merchantId), eq(orderId))).thenReturn(acceptedOrder);

        // 执行请求并验证响应
        mockMvc.perform(put("/merchants/orders/{orderId}/accept", orderId)
                        .param("merchantId", merchantId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证接受后的订单信息
                .andExpect(jsonPath("$.id").value(acceptedOrder.getId()))
                .andExpect(jsonPath("$.status").value(acceptedOrder.getStatus().toString()))
                .andExpect(jsonPath("$.totalPrice").value(acceptedOrder.getTotalPrice().doubleValue()))
                .andExpect(jsonPath("$.deliveryLocation").value(acceptedOrder.getDeliveryLocation()))
                .andExpect(jsonPath("$.orderTime").isNotEmpty())
                .andExpect(jsonPath("$.deliveryTime").isNotEmpty());

        // 验证 MerchantService.acceptOrder 方法被调用一次
        verify(merchantService, times(1)).acceptOrder(eq(merchantId), eq(orderId));
    }

    /**
     * 17. 测试接受订单失败（非法参数）
     * PUT /merchants/orders/{orderId}/accept
     */
    @Test
    @DisplayName("PUT /merchants/orders/{orderId}/accept - 接受订单失败（非法参数）")
    public void testAcceptOrder_BadRequest() throws Exception {
        // 准备数据
        Long orderId = 100L; // 假设不存在的 OrderId
        Long merchantId = -1L; // 假设非法 MerchantId

        // Mock MerchantService 的 acceptOrder 方法抛出 IllegalArgumentException
        when(merchantService.acceptOrder(eq(merchantId), eq(orderId)))
                .thenThrow(new IllegalArgumentException("订单未找到或商家无权限"));

        // 执行请求并验证响应
        mockMvc.perform(put("/merchants/orders/{orderId}/accept", orderId)
                        .param("merchantId", merchantId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // 验证 MerchantService.acceptOrder 方法被调用一次
        verify(merchantService, times(1)).acceptOrder(eq(merchantId), eq(orderId));
    }

    /**
     * 18. 测试请求送餐成功
     * PUT /merchants/orders/{orderId}/requestDelivery
     */
    @Test
    @DisplayName("PUT /merchants/orders/{orderId}/requestDelivery - 请求送餐成功")
    public void testRequestDelivery_Success() throws Exception {
        // 准备数据
        Long orderId = 1L;

        RequestDeliveryDTO requestDeliveryDTO = new RequestDeliveryDTO();
        requestDeliveryDTO.setDeliveryManId(2L);

        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(2L);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.REQUESTING_DELIVERY); // 使用新的枚举
        updatedOrder.setDeliveryMan(deliveryMan);
        updatedOrder.setTotalPrice(new BigDecimal("25.50"));
        updatedOrder.setDeliveryLocation("123 Delivery Street");
        updatedOrder.setOrderTime(new Date());
        updatedOrder.setDeliveryTime(new Date());

        // Mock MerchantService 的 requestDelivery 方法
        when(merchantService.requestDelivery(eq(orderId), eq(requestDeliveryDTO.getDeliveryManId())))
                .thenReturn(updatedOrder);

        // 执行请求并验证响应
        mockMvc.perform(put("/merchants/orders/{orderId}/requestDelivery", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDeliveryDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证更新后的订单信息
                .andExpect(jsonPath("$.id").value(updatedOrder.getId()))
                .andExpect(jsonPath("$.deliveryMan").value(updatedOrder.getDeliveryMan()))
                .andExpect(jsonPath("$.status").value(updatedOrder.getStatus().toString()))
                .andExpect(jsonPath("$.totalPrice").value(updatedOrder.getTotalPrice().doubleValue()))
                .andExpect(jsonPath("$.deliveryLocation").value(updatedOrder.getDeliveryLocation()))
                .andExpect(jsonPath("$.orderTime").isNotEmpty())
                .andExpect(jsonPath("$.deliveryTime").isNotEmpty());

        // 验证 MerchantService.requestDelivery 方法被调用一次
        verify(merchantService, times(1)).requestDelivery(eq(orderId), eq(requestDeliveryDTO.getDeliveryManId()));
    }

    /**
     * 19. 测试请求送餐失败（非法参数）
     * PUT /merchants/orders/{orderId}/requestDelivery
     */
    @Test
    @DisplayName("PUT /merchants/orders/{orderId}/requestDelivery - 请求送餐失败（非法参数）")
    public void testRequestDelivery_BadRequest() throws Exception {
        // 准备数据
        Long orderId = 100L; // 假设不存在的 OrderId

        RequestDeliveryDTO requestDeliveryDTO = new RequestDeliveryDTO();
        requestDeliveryDTO.setDeliveryManId(-1L); // 假设非法 DeliveryManId

        // Mock MerchantService 的 requestDelivery 方法抛出 IllegalArgumentException
        when(merchantService.requestDelivery(eq(orderId), eq(requestDeliveryDTO.getDeliveryManId())))
                .thenThrow(new IllegalArgumentException("订单未找到或送餐员无效"));

        // 执行请求并验证响应
        mockMvc.perform(put("/merchants/orders/{orderId}/requestDelivery", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDeliveryDTO)))
                .andExpect(status().isBadRequest());

        // 验证 MerchantService.requestDelivery 方法被调用一次
        verify(merchantService, times(1)).requestDelivery(eq(orderId), eq(requestDeliveryDTO.getDeliveryManId()));
    }

    /**
     * 20. 测试上传菜品图片成功
     * POST /merchants/images/upload
     */
    @Test
    @DisplayName("POST /merchants/images/upload - 上传菜品图片成功")
    public void testUploadImage_Success() throws Exception {
        // 准备数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Test Image Content".getBytes()
        );

        String imageUrl = "http://example.com/dish.jpg";

        // Mock ImageService 的 uploadImage 方法
        when(imageService.uploadImage(any(MultipartFile.class))).thenReturn(imageUrl);

        // 执行请求并验证响应
        mockMvc.perform(multipart("/merchants/images/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(content().string(imageUrl));

        // 验证 ImageService.uploadImage 方法被调用一次
        verify(imageService, times(1)).uploadImage(any(MultipartFile.class));
    }

    /**
     * 21. 测试上传菜品图片失败（非法参数）
     * POST /merchants/images/upload
     */
    @Test
    @DisplayName("POST /merchants/images/upload - 上传菜品图片失败（非法参数）")
    public void testUploadImage_BadRequest() throws Exception {
        // 准备数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Test Image Content".getBytes()
        );

        // Mock ImageService 的 uploadImage 方法抛出异常
        when(imageService.uploadImage(any(MultipartFile.class)))
                .thenThrow(new IllegalArgumentException("无效的图片文件"));

        // 执行请求并验证响应
        mockMvc.perform(multipart("/merchants/images/upload")
                        .file(file))
                .andExpect(status().isBadRequest());

        // 验证 ImageService.uploadImage 方法被调用一次
        verify(imageService, times(1)).uploadImage(any(MultipartFile.class));
    }
}
