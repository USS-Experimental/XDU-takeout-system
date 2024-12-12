package com.github.ussexperimental.takeoutsystem.controller;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.dto.UserDTO;
import com.github.ussexperimental.takeoutsystem.dto.UserUpdateDTO;
import com.github.ussexperimental.takeoutsystem.entity.User;
import com.github.ussexperimental.takeoutsystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 创建用户
     * POST /admin/users
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO) {
        try {
            User user = adminService.createUser(
                    userDTO.getUsername(),
                    userDTO.getPassword(),
                    userDTO.getPhone(),
                    userDTO.getEmail(),
                    userDTO.getAddress(),
                    userDTO.getRoleType()
            );
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 获取所有用户
     * GET /admin/users?page={page}&size={size}
     */
    @GetMapping
    public ResponseEntity<PageResponse<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PageResponse<User> response = adminService.getAllUsers(page, size);
            if (response.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取单个用户
     * GET /admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        try {
            User user = adminService.getUserById(userId);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 更新用户
     * PUT /admin/users/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateDTO userUpdateDTO
    ) {
        try {
            User updatedUser = adminService.updateUser(
                    userId,
                    userUpdateDTO.getPhone(),
                    userUpdateDTO.getEmail(),
                    userUpdateDTO.getAddress(),
                    userUpdateDTO.getRoleType()
            );
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除用户
     * DELETE /admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            adminService.deleteUser(userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
