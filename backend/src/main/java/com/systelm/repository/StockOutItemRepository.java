package com.systelm.repository;

import com.systelm.entity.StockOutItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockOutItemRepository extends JpaRepository<StockOutItem, Long> {}
