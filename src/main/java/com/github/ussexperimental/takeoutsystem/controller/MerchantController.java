package com.github.ussexperimental.takeoutsystem.controller;

import com.github.ussexperimental.takeoutsystem.dto.DishCreateDTO;
import com.github.ussexperimental.takeoutsystem.dto.DishUpdateDTO;
import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.dto.RequestDeliveryDTO;
import com.github.ussexperimental.takeoutsystem.entity.Dish;
import com.github.ussexperimental.takeoutsystem.entity.Order;
import com.github.ussexperimental.takeoutsystem.service.ImageService;
import com.github.ussexperimental.takeoutsystem.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/merchants")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ImageService imageService;

    /**
     * 创建菜品
     * POST /merchants/menu
     */
    @PostMapping("/menu")
    public ResponseEntity<Dish> createDish(@RequestBody DishCreateDTO dishCreateDTO) {
        try {
            Dish dish = merchantService.addDish(
                    dishCreateDTO.getMerchantId(),
                    dishCreateDTO.getName(),
                    dishCreateDTO.getPrice(),
                    dishCreateDTO.getDescription(),
                    dishCreateDTO.getImageUrl()
            );
            return new ResponseEntity<>(dish, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 获取菜单
     * GET /merchants/menu?page={page}&size={size}
     */
    @GetMapping("/menu")
    public ResponseEntity<PageResponse<Dish>> getMenu(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PageResponse<Dish> menu = merchantService.getMenu(merchantId, page, size);
            if (menu.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(menu, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 更新菜品
     * PUT /merchants/menu/{dishId}
     */
    @PutMapping("/menu/{dishId}")
    public ResponseEntity<Dish> updateDish(
            @PathVariable Long dishId,
            @RequestBody DishUpdateDTO dishUpdateDTO
    ) {
        try {
            Dish updatedDish = merchantService.updateDish(
                    dishId,
                    dishUpdateDTO.getName(),
                    dishUpdateDTO.getPrice(),
                    dishUpdateDTO.getDescription(),
                    dishUpdateDTO.getImageUrl()
            );
            return new ResponseEntity<>(updatedDish, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除菜品
     * DELETE /merchants/menu/{dishId}
     */
    @DeleteMapping("/menu/{dishId}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long dishId) {
        try {
            merchantService.deleteDish(dishId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 查看销售情况
     * GET /merchants/sales?merchantId={merchantId}&startDate={startDate}&endDate={endDate}&page={page}&size={size}
     */
    @GetMapping("/sales")
    public ResponseEntity<PageResponse<Order>> viewSales(
            @RequestParam Long merchantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Date start = startDate != null ? new Date(Long.parseLong(startDate)) : null;
            Date end = endDate != null ? new Date(Long.parseLong(endDate)) : null;
            PageResponse<Order> sales = merchantService.viewSales(merchantId, start, end, page, size);
            if (sales.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(sales, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 查看待确认订单
     * GET /merchants/orders/pending?page={page}&size={size}
     */
    @GetMapping("/orders/pending")
    public ResponseEntity<PageResponse<Order>> viewPendingOrders(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PageResponse<Order> pendingOrders = merchantService.viewPendingOrders(merchantId, page, size);
            if (pendingOrders.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(pendingOrders, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 接受订单
     * PUT /merchants/orders/{orderId}/accept
     */
    @PutMapping("/orders/{orderId}/accept")
    public ResponseEntity<Order> acceptOrder(
            @PathVariable Long orderId,
            @RequestParam Long merchantId
    ) {
        try {
            Order order = merchantService.acceptOrder(merchantId, orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 请求送餐
     * PUT /merchants/orders/{orderId}/requestDelivery
     */
    @PutMapping("/orders/{orderId}/requestDelivery")
    public ResponseEntity<Order> requestDelivery(
            @PathVariable Long orderId,
            @RequestBody RequestDeliveryDTO requestDeliveryDTO
    ) {
        try {
            Order order = merchantService.requestDelivery(
                    orderId,
                    requestDeliveryDTO.getDeliveryManId()
            );
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 上传菜品图片
     * POST /merchants/images/upload
     */
    @PostMapping("/images/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageService.uploadImage(file);
            return new ResponseEntity<>(imageUrl, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IOException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
