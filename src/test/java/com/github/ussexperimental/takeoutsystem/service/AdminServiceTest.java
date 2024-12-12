package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.Customer;
import com.github.ussexperimental.takeoutsystem.entity.Merchant;
import com.github.ussexperimental.takeoutsystem.entity.Role;
import com.github.ussexperimental.takeoutsystem.entity.User;
import com.github.ussexperimental.takeoutsystem.entity.enums.RoleType;
import com.github.ussexperimental.takeoutsystem.entity.enums.UserType;
import com.github.ussexperimental.takeoutsystem.repository.RoleRepository;
import com.github.ussexperimental.takeoutsystem.repository.UserRepository;
import com.github.ussexperimental.takeoutsystem.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminServiceTest {

    @InjectMocks
    private AdminServiceImpl adminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("测试创建用户 - 成功")
    public void testCreateUser_Success() {
        // 准备数据
        String username = "newuser";
        String password = "password123";
        String phone = "1234567890";
        String email = "newuser@example.com";
        String address = "123 Main St";
        RoleType roleType = RoleType.CUSTOMER;

        Role role = new Role();
        role.setId(1L);
        role.setRoleType(roleType);

        // 模拟仓库行为
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(roleRepository.findByRoleType(roleType)).thenReturn(Optional.of(role));

        // 模拟保存行为
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // 调用方法
        User createdUser = adminService.createUser(username, password, phone, email, address, roleType);

        // 验证
        assertNotNull(createdUser);
        assertEquals(1L, createdUser.getId());
        assertEquals(username, createdUser.getUsername());
        assertEquals(password, createdUser.getPassword());
        assertEquals(phone, createdUser.getPhone());
        assertEquals(email, createdUser.getEmail());
        assertEquals(address, createdUser.getAddress());
        assertEquals(role, createdUser.getRole());
        assertEquals(UserType.CUSTOMER, createdUser.getUserType());

        verify(userRepository, times(1)).existsByUsername(username);
        verify(roleRepository, times(1)).findByRoleType(roleType);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(username, savedUser.getUsername());
        assertEquals(password, savedUser.getPassword());
    }

    @Test
    @DisplayName("测试创建用户 - 用户名已存在")
    public void testCreateUser_UsernameExists() {
        // 准备数据
        String username = "existinguser";
        String password = "password123";
        String phone = "1234567890";
        String email = "existinguser@example.com";
        String address = "123 Main St";
        RoleType roleType = RoleType.MERCHANT;

        // 模拟仓库行为
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.createUser(username, password, phone, email, address, roleType);
        });

        assertEquals("用户名已存在", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername(username);
        verify(roleRepository, never()).findByRoleType(any(RoleType.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试创建用户 - 角色不存在")
    public void testCreateUser_RoleNotFound() {
        // 准备数据
        String username = "newuser";
        String password = "password123";
        String phone = "1234567890";
        String email = "newuser@example.com";
        String address = "123 Main St";
        RoleType roleType = RoleType.DELIVERYMAN;

        // 模拟仓库行为
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(roleRepository.findByRoleType(roleType)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.createUser(username, password, phone, email, address, roleType);
        });

        assertEquals("角色不存在", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername(username);
        verify(roleRepository, times(1)).findByRoleType(roleType);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试获取所有用户 - 成功")
    public void testGetAllUsers_Success() {
        // 准备数据
        int page = 0;
        int size = 10;

        User user1 = new Customer();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new Merchant();
        user2.setId(2L);
        user2.setUsername("user2");

        List<User> users = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(page, size), users.size());

        // 模拟仓库行为
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        // 调用方法
        PageResponse<User> response = adminService.getAllUsers(page, size);

        // 验证
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("测试根据用户ID获取用户 - 成功")
    public void testGetUserById_Success() {
        // 准备数据
        Long userId = 1L;
        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.CUSTOMER);

        User user = new Customer(); // 假设存在 Admin 类继承自 User
        user.setId(userId);
        user.setUsername("newcustomer");
        user.setRole(role);
        user.setUserType(UserType.CUSTOMER);

        // 模拟仓库行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 调用方法
        User foundUser = adminService.getUserById(userId);

        // 验证
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals("newcustomer", foundUser.getUsername());
        assertEquals(role, foundUser.getRole());
        assertEquals(UserType.CUSTOMER, foundUser.getUserType());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("测试根据用户ID获取用户 - 用户不存在")
    public void testGetUserById_UserNotFound() {
        // 准备数据
        Long userId = 100L;

        // 模拟仓库行为
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.getUserById(userId);
        });

        assertEquals("用户不存在", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("测试更新用户 - 成功")
    public void testUpdateUser_Success() {
        // 准备数据
        Long userId = 1L;
        String newPhone = "0987654321";
        String newEmail = "updated@example.com";
        String newAddress = "456 New St";
        RoleType newRoleType = RoleType.MERCHANT;

        Role oldRole = new Role();
        oldRole.setId(1L);
        oldRole.setRoleType(RoleType.CUSTOMER);

        Role newRole = new Role();
        newRole.setId(2L);
        newRole.setRoleType(newRoleType);

        User user = new Customer();
        user.setId(userId);
        user.setUsername("testuser");
        user.setPhone("1234567890");
        user.setEmail("testuser@example.com");
        user.setAddress("123 Main St");
        user.setRole(oldRole);
        user.setUserType(UserType.CUSTOMER);

        // 模拟仓库行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleType(newRoleType)).thenReturn(Optional.of(newRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 调用方法
        User updatedUser = adminService.updateUser(userId, newPhone, newEmail, newAddress, newRoleType);

        // 验证
        assertNotNull(updatedUser);
        assertEquals(newPhone, updatedUser.getPhone());
        assertEquals(newEmail, updatedUser.getEmail());
        assertEquals(newAddress, updatedUser.getAddress());
        assertEquals(newRole, updatedUser.getRole());
        assertEquals(UserType.MERCHANT, updatedUser.getUserType());

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findByRoleType(newRoleType);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("测试更新用户 - 用户不存在")
    public void testUpdateUser_UserNotFound() {
        // 准备数据
        Long userId = 100L;
        String newPhone = "0987654321";
        String newEmail = "updated@example.com";
        String newAddress = "456 New St";
        RoleType newRoleType = RoleType.ADMIN;

        // 模拟仓库行为
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.updateUser(userId, newPhone, newEmail, newAddress, newRoleType);
        });

        assertEquals("用户不存在", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, never()).findByRoleType(any(RoleType.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试更新用户 - 角色不存在")
    public void testUpdateUser_RoleNotFound() {
        // 准备数据
        Long userId = 1L;
        String newPhone = "0987654321";
        String newEmail = "updated@example.com";
        String newAddress = "456 New St";
        RoleType newRoleType = RoleType.DELIVERYMAN;

        Role oldRole = new Role();
        oldRole.setId(1L);
        oldRole.setRoleType(RoleType.CUSTOMER);

        User user = new Customer();
        user.setId(userId);
        user.setUsername("testuser");
        user.setPhone("1234567890");
        user.setEmail("testuser@example.com");
        user.setAddress("123 Main St");
        user.setRole(oldRole);
        user.setUserType(UserType.CUSTOMER);

        // 模拟仓库行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleType(newRoleType)).thenReturn(Optional.empty());

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.updateUser(userId, newPhone, newEmail, newAddress, newRoleType);
        });

        assertEquals("角色不存在", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findByRoleType(newRoleType);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试删除用户 - 成功")
    public void testDeleteUser_Success() {
        // 准备数据
        Long userId = 1L;

        // 模拟仓库行为
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // 调用方法
        adminService.deleteUser(userId);

        // 验证
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("测试删除用户 - 用户不存在")
    public void testDeleteUser_UserNotFound() {
        // 准备数据
        Long userId = 100L;

        // 模拟仓库行为
        when(userRepository.existsById(userId)).thenReturn(false);

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.deleteUser(userId);
        });

        assertEquals("用户不存在", exception.getMessage());

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(anyLong());
    }

    // 根据需要添加更多测试方法，例如测试 getAllUsers 的不同场景
}

