package com.github.ussexperimental.takeoutsystem.entity;

import com.github.ussexperimental.takeoutsystem.entity.enums.RoleType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @OneToMany(mappedBy = "role")
    private List<User> users;
}
