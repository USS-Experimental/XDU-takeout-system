package com.github.ussexperimental.takeoutsystem.repository;

import com.github.ussexperimental.takeoutsystem.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
}

