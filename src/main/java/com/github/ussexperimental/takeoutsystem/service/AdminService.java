package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.dto.PageResponse;
import com.github.ussexperimental.takeoutsystem.entity.User;
import com.github.ussexperimental.takeoutsystem.entity.enums.RoleType;
import org.springframework.stereotype.Service;

@Service
public interface AdminService {

    User createUser(String username,
                           String password,
                           String phone,
                           String email,
                           String address,
                           RoleType roleType);

    PageResponse<User> getAllUsers(int page, int size);

    User getUserById(Long userId);

    User updateUser(Long userId,
                    String phone,
                    String email,
                    String address,
                    RoleType roleType);

    void deleteUser(Long userId);
}
