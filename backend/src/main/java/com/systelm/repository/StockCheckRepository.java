package com.systelm.repository;

import com.systelm.entity.StockCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockCheckRepository extends JpaRepository<StockCheck, Long> {
    long countByDocNoStartingWith(String prefix);
}
