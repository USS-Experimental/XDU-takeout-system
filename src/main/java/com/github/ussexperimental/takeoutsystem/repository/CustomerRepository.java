package com.github.ussexperimental.takeoutsystem.repository;

import com.github.ussexperimental.takeoutsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
