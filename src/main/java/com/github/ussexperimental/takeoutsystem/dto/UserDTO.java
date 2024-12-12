package com.github.ussexperimental.takeoutsystem.dto;

import com.github.ussexperimental.takeoutsystem.entity.enums.RoleType;
import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String phone;
    private String email;
    private String address;
    private RoleType roleType;
}

