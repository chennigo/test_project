package com.systelm.repository;

import com.systelm.entity.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockInRepository extends JpaRepository<StockIn, Long> {
    long countByDocNoStartingWith(String prefix);
}
