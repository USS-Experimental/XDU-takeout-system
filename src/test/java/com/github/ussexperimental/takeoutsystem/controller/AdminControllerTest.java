package com.github.ussexperimental.takeoutsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.dto.UserDTO;
import com.github.ussexperimental.takeoutsystem.dto.UserUpdateDTO;
import com.github.ussexperimental.takeoutsystem.entity.Role;
import com.github.ussexperimental.takeoutsystem.entity.User;
import com.github.ussexperimental.takeoutsystem.entity.enums.RoleType;
import com.github.ussexperimental.takeoutsystem.entity.enums.UserType;
import com.github.ussexperimental.takeoutsystem.service.AdminService;
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

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();
    }

    // 1. 测试创建用户
    @Test
    @DisplayName("POST /admin/users - 创建用户成功")
    public void testCreateUser_Success() throws Exception {
        // 准备数据
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password123");
        userDTO.setPhone("1234567890");
        userDTO.setEmail("test@example.com");
        userDTO.setAddress("123 Test Street");
        userDTO.setRoleType(RoleType.CUSTOMER); // 例如，"ADMIN" 或 "USER"

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleType(RoleType.CUSTOMER);

        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setUsername("testuser");
        createdUser.setPassword("password123"); // 在实际应用中，密码应加密
        createdUser.setPhone("1234567890");
        createdUser.setEmail("test@example.com");
        createdUser.setAddress("123 Test Street");
        createdUser.setRole(userRole);
        createdUser.setUserType(UserType.CUSTOMER);

        // Mock AdminService 的 createUser 方法
        when(adminService.createUser(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(RoleType.class) // 修正这里
        )).thenReturn(createdUser);

        // 执行请求并验证响应
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.username").value(createdUser.getUsername()))
                .andExpect(jsonPath("$.password").value(createdUser.getPassword()))
                .andExpect(jsonPath("$.phone").value(createdUser.getPhone()))
                .andExpect(jsonPath("$.email").value(createdUser.getEmail()))
                .andExpect(jsonPath("$.address").value(createdUser.getAddress()))
                .andExpect(jsonPath("$.userType").value(createdUser.getUserType().toString()))
                // 验证 Role 对象
                .andExpect(jsonPath("$.role.id").value(userRole.getId()))
                .andExpect(jsonPath("$.role.roleType").value(userRole.getRoleType().toString()));

        // 验证 AdminService.createUser 方法被调用一次
        verify(adminService, times(1)).createUser(
                eq(userDTO.getUsername()),
                eq(userDTO.getPassword()),
                eq(userDTO.getPhone()),
                eq(userDTO.getEmail()),
                eq(userDTO.getAddress()),
                eq(userDTO.getRoleType())
        );
    }

    @Test
    @DisplayName("POST /admin/users - 创建用户失败（非法参数）")
    public void testCreateUser_BadRequest() throws Exception {
        // 准备数据
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(""); // 例如，用户名为空
        userDTO.setPassword("password123");
        userDTO.setPhone("1234567890");
        userDTO.setEmail("test@example.com");
        userDTO.setAddress("123 Test Street");
        userDTO.setRoleType(RoleType.CUSTOMER); // 例如，"ADMIN" 或 "USER"

        // Mock AdminService 的 createUser 方法抛出 IllegalArgumentException
        when(adminService.createUser(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(RoleType.class) // 修正这里
        )).thenThrow(new IllegalArgumentException("非法参数"));

        // 执行请求并验证响应
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());

        // 验证 AdminService.createUser 方法被调用一次
        verify(adminService, times(1)).createUser(
                eq(userDTO.getUsername()),
                eq(userDTO.getPassword()),
                eq(userDTO.getPhone()),
                eq(userDTO.getEmail()),
                eq(userDTO.getAddress()),
                eq(userDTO.getRoleType())
        );
    }

    // 2. 测试获取所有用户
    @Test
    @DisplayName("GET /admin/users - 获取所有用户成功")
    public void testGetAllUsers_Success() throws Exception {
        // 准备数据
        Role customerRole = new Role();
        customerRole.setId(1L);
        customerRole.setRoleType(RoleType.CUSTOMER);

        Role merchantRole = new Role();
        merchantRole.setId(2L);
        merchantRole.setRoleType(RoleType.MERCHANT);

        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setPassword("pass1");
        user1.setPhone("1111111111");
        user1.setEmail("user1@example.com");
        user1.setAddress("Address 1");
        user1.setRole(customerRole);
        user1.setUserType(UserType.CUSTOMER);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setPassword("pass2");
        user2.setPhone("2222222222");
        user2.setEmail("user2@example.com");
        user2.setAddress("Address 2");
        user2.setRole(merchantRole);
        user2.setUserType(UserType.MERCHANT);

        PageResponse<User> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(user1, user2));
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);
        pageResponse.setLast(true);

        // Mock AdminService 的 getAllUsers 方法
        when(adminService.getAllUsers(anyInt(), anyInt())).thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/admin/users")
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
                // 验证第一个用户
                .andExpect(jsonPath("$.content[0].id").value(user1.getId()))
                .andExpect(jsonPath("$.content[0].username").value(user1.getUsername()))
                .andExpect(jsonPath("$.content[0].password").value(user1.getPassword()))
                .andExpect(jsonPath("$.content[0].phone").value(user1.getPhone()))
                .andExpect(jsonPath("$.content[0].email").value(user1.getEmail()))
                .andExpect(jsonPath("$.content[0].address").value(user1.getAddress()))
                .andExpect(jsonPath("$.content[0].userType").value(user1.getUserType().toString()))
                // 验证第一个用户的 Role 对象
                .andExpect(jsonPath("$.content[0].role.id").value(customerRole.getId()))
                .andExpect(jsonPath("$.content[0].role.roleType").value(customerRole.getRoleType().toString()))
                // 验证第二个用户
                .andExpect(jsonPath("$.content[1].id").value(user2.getId()))
                .andExpect(jsonPath("$.content[1].username").value(user2.getUsername()))
                .andExpect(jsonPath("$.content[1].password").value(user2.getPassword()))
                .andExpect(jsonPath("$.content[1].phone").value(user2.getPhone()))
                .andExpect(jsonPath("$.content[1].email").value(user2.getEmail()))
                .andExpect(jsonPath("$.content[1].address").value(user2.getAddress()))
                .andExpect(jsonPath("$.content[1].userType").value(user2.getUserType().toString()))
                // 验证第二个用户的 Role 对象
                .andExpect(jsonPath("$.content[1].role.id").value(merchantRole.getId()))
                .andExpect(jsonPath("$.content[1].role.roleType").value(merchantRole.getRoleType().toString()));

        // 验证 AdminService.getAllUsers 方法被调用一次
        verify(adminService, times(1)).getAllUsers(0, 10);
    }

    @Test
    @DisplayName("GET /admin/users - 获取所有用户无内容")
    public void testGetAllUsers_NoContent() throws Exception {
        // 准备数据
        PageResponse<User> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList());
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(0);
        pageResponse.setTotalPages(0);
        pageResponse.setLast(true);

        // Mock AdminService 的 getAllUsers 方法
        when(adminService.getAllUsers(anyInt(), anyInt())).thenReturn(pageResponse);

        // 执行请求并验证响应
        mockMvc.perform(get("/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 验证 AdminService.getAllUsers 方法被调用一次
        verify(adminService, times(1)).getAllUsers(0, 10);
    }

    @Test
    @DisplayName("GET /admin/users - 获取所有用户内部服务器错误")
    public void testGetAllUsers_InternalServerError() throws Exception {
        // Mock AdminService 的 getAllUsers 方法抛出异常
        when(adminService.getAllUsers(anyInt(), anyInt())).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行请求并验证响应
        mockMvc.perform(get("/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        // 验证 AdminService.getAllUsers 方法被调用一次
        verify(adminService, times(1)).getAllUsers(0, 10);
    }

    // 3. 测试获取单个用户
    @Test
    @DisplayName("GET /admin/users/{userId} - 获取用户成功")
    public void testGetUserById_Success() throws Exception {
        // 准备数据
        Long userId = 1L;
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleType(RoleType.CUSTOMER);

        User user = new User();
        user.setId(userId);
        user.setUsername("user1");
        user.setPassword("pass1");
        user.setPhone("1111111111");
        user.setEmail("user1@example.com");
        user.setAddress("Address 1");
        user.setRole(userRole);
        user.setUserType(UserType.CUSTOMER);

        // Mock AdminService 的 getUserById 方法
        when(adminService.getUserById(userId)).thenReturn(user);

        // 执行请求并验证响应
        mockMvc.perform(get("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证用户信息
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.password").value(user.getPassword()))
                .andExpect(jsonPath("$.phone").value(user.getPhone()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.address").value(user.getAddress()))
                .andExpect(jsonPath("$.userType").value(user.getUserType().toString()))
                // 验证 Role 对象
                .andExpect(jsonPath("$.role.id").value(userRole.getId()))
                .andExpect(jsonPath("$.role.roleType").value(userRole.getRoleType().toString()));

        // 验证 AdminService.getUserById 方法被调用一次
        verify(adminService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("GET /admin/users/{userId} - 获取用户失败（未找到）")
    public void testGetUserById_NotFound() throws Exception {
        // 准备数据
        Long userId = 100L;

        // Mock AdminService 的 getUserById 方法抛出 IllegalArgumentException
        when(adminService.getUserById(userId)).thenThrow(new IllegalArgumentException("用户未找到"));

        // 执行请求并验证响应
        mockMvc.perform(get("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // 验证 AdminService.getUserById 方法被调用一次
        verify(adminService, times(1)).getUserById(userId);
    }

    // 4. 测试更新用户
    @Test
    @DisplayName("PUT /admin/users/{userId} - 更新用户成功")
    public void testUpdateUser_Success() throws Exception {
        // 准备数据
        Long userId = 1L;
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setPhone("9999999999");
        userUpdateDTO.setEmail("updated@example.com");
        userUpdateDTO.setAddress("Updated Address");
        userUpdateDTO.setRoleType(RoleType.CUSTOMER); // 例如，"ADMIN" 或 "USER"

        Role updatedRole = new Role();
        updatedRole.setId(2L);
        updatedRole.setRoleType(RoleType.CUSTOMER);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("user1");
        updatedUser.setPassword("pass1");
        updatedUser.setPhone("9999999999");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setAddress("Updated Address");
        updatedUser.setRole(updatedRole);
        updatedUser.setUserType(UserType.CUSTOMER);

        // Mock AdminService 的 updateUser 方法
        when(adminService.updateUser(
                eq(userId),
                eq(userUpdateDTO.getPhone()),
                eq(userUpdateDTO.getEmail()),
                eq(userUpdateDTO.getAddress()),
                eq(userUpdateDTO.getRoleType())
        )).thenReturn(updatedUser);

        // 执行请求并验证响应
        mockMvc.perform(put("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 验证更新后的用户信息
                .andExpect(jsonPath("$.id").value(updatedUser.getId()))
                .andExpect(jsonPath("$.username").value(updatedUser.getUsername()))
                .andExpect(jsonPath("$.password").value(updatedUser.getPassword()))
                .andExpect(jsonPath("$.phone").value(updatedUser.getPhone()))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.address").value(updatedUser.getAddress()))
                .andExpect(jsonPath("$.userType").value(updatedUser.getUserType().toString()))
                // 验证 Role 对象
                .andExpect(jsonPath("$.role.id").value(updatedRole.getId()))
                .andExpect(jsonPath("$.role.roleType").value(updatedRole.getRoleType().toString()));

        // 验证 AdminService.updateUser 方法被调用一次
        verify(adminService, times(1)).updateUser(
                eq(userId),
                eq(userUpdateDTO.getPhone()),
                eq(userUpdateDTO.getEmail()),
                eq(userUpdateDTO.getAddress()),
                eq(userUpdateDTO.getRoleType())
        );
    }

    @Test
    @DisplayName("PUT /admin/users/{userId} - 更新用户失败（未找到）")
    public void testUpdateUser_NotFound() throws Exception {
        // 准备数据
        Long userId = 100L;
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setPhone("9999999999");
        userUpdateDTO.setEmail("updated@example.com");
        userUpdateDTO.setAddress("Updated Address");
        userUpdateDTO.setRoleType(RoleType.CUSTOMER); // 例如，"ADMIN" 或 "USER"

        // Mock AdminService 的 updateUser 方法抛出 IllegalArgumentException
        when(adminService.updateUser(
                eq(userId),
                eq(userUpdateDTO.getPhone()),
                eq(userUpdateDTO.getEmail()),
                eq(userUpdateDTO.getAddress()),
                eq(userUpdateDTO.getRoleType())
        )).thenThrow(new IllegalArgumentException("用户未找到"));

        // 执行请求并验证响应
        mockMvc.perform(put("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isNotFound());

        // 验证 AdminService.updateUser 方法被调用一次
        verify(adminService, times(1)).updateUser(
                eq(userId),
                eq(userUpdateDTO.getPhone()),
                eq(userUpdateDTO.getEmail()),
                eq(userUpdateDTO.getAddress()),
                eq(userUpdateDTO.getRoleType())
        );
    }

    // 5. 测试删除用户
    @Test
    @DisplayName("DELETE /admin/users/{userId} - 删除用户成功")
    public void testDeleteUser_Success() throws Exception {
        // 准备数据
        Long userId = 1L;

        // Mock AdminService 的 deleteUser 方法（无需返回值）
        doNothing().when(adminService).deleteUser(userId);

        // 执行请求并验证响应
        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());

        // 验证 AdminService.deleteUser 方法被调用一次
        verify(adminService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("DELETE /admin/users/{userId} - 删除用户失败（未找到）")
    public void testDeleteUser_NotFound() throws Exception {
        // 准备数据
        Long userId = 100L;

        // Mock AdminService 的 deleteUser 方法抛出 IllegalArgumentException
        doThrow(new IllegalArgumentException("用户未找到")).when(adminService).deleteUser(userId);

        // 执行请求并验证响应
        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNotFound());

        // 验证 AdminService.deleteUser 方法被调用一次
        verify(adminService, times(1)).deleteUser(userId);
    }
}
