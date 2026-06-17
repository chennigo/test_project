package com.systelm.repository;

import com.systelm.entity.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {
    List<ProductBatch> findByProductId(Long productId);
}
