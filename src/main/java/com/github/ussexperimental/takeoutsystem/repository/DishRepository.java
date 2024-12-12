package com.github.ussexperimental.takeoutsystem.repository;

import com.github.ussexperimental.takeoutsystem.entity.Dish;
import com.github.ussexperimental.takeoutsystem.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    Page<Dish> findByMerchant(Merchant merchant, Pageable pageable);

    List<Dish> findByNameContaining(String name);

    List<Dish> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}

