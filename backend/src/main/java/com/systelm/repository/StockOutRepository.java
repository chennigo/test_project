package com.systelm.repository;

import com.systelm.entity.StockOut;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockOutRepository extends JpaRepository<StockOut, Long> {
    long countByDocNoStartingWith(String prefix);
}
