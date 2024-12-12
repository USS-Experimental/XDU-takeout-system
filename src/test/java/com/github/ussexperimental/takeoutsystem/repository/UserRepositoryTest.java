package com.github.ussexperimental.takeoutsystem.repository;

import com.github.ussexperimental.takeoutsystem.entity.Customer;
import com.github.ussexperimental.takeoutsystem.entity.Merchant;
import com.github.ussexperimental.takeoutsystem.entity.Role;
import com.github.ussexperimental.takeoutsystem.entity.User;
import com.github.ussexperimental.takeoutsystem.entity.enums.RoleType;
import com.github.ussexperimental.takeoutsystem.entity.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("测试根据用户名查找用户")
    public void testFindByUsername() {
        // 先创建并保存一个角色
        Role role = new Role();
        role.setRoleType(RoleType.CUSTOMER);
        roleRepository.save(role);

        // 创建并保存一个用户
        Customer customer = new Customer();
        customer.setUsername("testuser");
        customer.setPassword("password123");
        customer.setRole(role);
        customer.setUserType(UserType.CUSTOMER);
        userRepository.save(customer);

        // 测试查找
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    @DisplayName("测试保存和删除用户")
    public void testSaveAndDeleteUser() {
        // 创建并保存角色
        Role role = new Role();
        role.setRoleType(RoleType.MERCHANT);
        roleRepository.save(role);

        // 创建并保存用户
        Merchant merchant = new Merchant();
        merchant.setUsername("merchant1");
        merchant.setPassword("password456");
        merchant.setRole(role);
        merchant.setUserType(UserType.MERCHANT);
        userRepository.save(merchant);

        // 验证保存
        Optional<User> foundMerchant = userRepository.findByUsername("merchant1");
        assertTrue(foundMerchant.isPresent());

        // 删除用户
        userRepository.delete(merchant);

        // 验证删除
        Optional<User> deletedMerchant = userRepository.findByUsername("merchant1");
        assertFalse(deletedMerchant.isPresent());
    }
}
