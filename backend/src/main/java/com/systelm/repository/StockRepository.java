package com.systelm.repository;

import com.systelm.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByWarehouseIdAndProductIdAndBatchId(Long warehouseId, Long productId, Long batchId);

    List<Stock> findByWarehouseId(Long warehouseId);

    List<Stock> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}
