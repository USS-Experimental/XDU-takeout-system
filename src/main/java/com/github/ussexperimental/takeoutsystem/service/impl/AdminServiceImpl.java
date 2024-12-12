package com.github.ussexperimental.takeoutsystem.service.impl;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.*;
import com.github.ussexperimental.takeoutsystem.entity.enums.RoleType;
import com.github.ussexperimental.takeoutsystem.entity.enums.UserType;
import com.github.ussexperimental.takeoutsystem.repository.RoleRepository;
import com.github.ussexperimental.takeoutsystem.repository.UserRepository;
import com.github.ussexperimental.takeoutsystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public User createUser(String username, String password, String phone, String email, String address, RoleType roleType) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        Role role = roleRepository.findByRoleType(roleType)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在"));

        User user = switch (roleType) {
            case CUSTOMER -> new Customer();
            case MERCHANT -> new Merchant();
            case DELIVERYMAN -> new DeliveryMan();
            default -> throw new IllegalArgumentException("无效的角色类型");
        };

        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setEmail(email);
        user.setAddress(address);
        user.setRole(role);
        user.setUserType(convertRoleToUserType(roleType));

        return userRepository.save(user);
    }

    public PageResponse<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> userPage = userRepository.findAll(pageable);
        return new PageResponse<>(
                userPage.getContent(),
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    @Transactional
    public User updateUser(Long userId, String phone, String email, String address, RoleType roleType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (phone != null && !phone.isEmpty()) {
            user.setPhone(phone);
        }
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (address != null && !address.isEmpty()) {
            user.setAddress(address);
        }
        if (roleType != null) {
            Role role = roleRepository.findByRoleType(roleType)
                    .orElseThrow(() -> new IllegalArgumentException("角色不存在"));
            user.setRole(role);
            user.setUserType(convertRoleToUserType(roleType));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("用户不存在");
        }
        userRepository.deleteById(userId);
    }

    private UserType convertRoleToUserType(RoleType roleType) {
        return switch (roleType) {
            case CUSTOMER -> UserType.CUSTOMER;
            case MERCHANT -> UserType.MERCHANT;
            case DELIVERYMAN -> UserType.DELIVERYMAN;
            default -> throw new IllegalArgumentException("无效的角色类型");
        };
    }
}
