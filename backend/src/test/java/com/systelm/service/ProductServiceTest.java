package com.systelm.service;

import com.systelm.dto.CreateBatchCommand;
import com.systelm.dto.CreateProductCommand;
import com.systelm.entity.Product;
import com.systelm.entity.ProductBatch;
import com.systelm.entity.Warehouse;
import com.systelm.repository.UserAccountRepository;
import com.systelm.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired ProductService productService;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired UserAccountRepository userAccountRepository;

    @Test
    void createProduct_withBatch_persistsBoth() {
        Product product = productService.createProduct(
            new CreateProductCommand("矿泉水", uniqueSku(), 2.0, 3.0, "瓶", 10.0));
        ProductBatch batch = productService.createBatch(
            new CreateBatchCommand(product.getId(), "B20260101", "2026-01-01"));
        assertNotNull(batch.getId());
        assertEquals("B20260101", batch.getBatchNo());
    }

    private String uniqueSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
