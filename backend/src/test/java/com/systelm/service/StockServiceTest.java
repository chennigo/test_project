package com.systelm.service;

import com.systelm.dto.CreateBatchCommand;
import com.systelm.dto.CreateProductCommand;
import com.systelm.entity.Product;
import com.systelm.entity.ProductBatch;
import com.systelm.entity.Warehouse;
import com.systelm.exception.InsufficientStockException;
import com.systelm.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StockServiceTest {

    @Autowired StockService stockService;
    @Autowired ProductService productService;
    @Autowired WarehouseRepository warehouseRepository;

    private Long warehouseId;
    private Long productId;
    private Long batchId;

    @BeforeEach
    void setUp() {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("测试仓-" + UUID.randomUUID().toString().substring(0, 6));
        warehouse.setStatus("ACTIVE");
        warehouseId = warehouseRepository.save(warehouse).getId();

        Product product = productService.createProduct(
            new CreateProductCommand("测试商品", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                1.0, 2.0, "件", 0.0));
        productId = product.getId();

        ProductBatch batch = productService.createBatch(
            new CreateBatchCommand(productId, "B-" + UUID.randomUUID().toString().substring(0, 6), "2026-01-01"));
        batchId = batch.getId();
    }

    @Test
    void increaseStock_createsOrUpdatesQuantity() {
        stockService.increase(warehouseId, productId, batchId, 100);
        assertEquals(100, stockService.getQuantity(warehouseId, productId, batchId));
        stockService.increase(warehouseId, productId, batchId, 50);
        assertEquals(150, stockService.getQuantity(warehouseId, productId, batchId));
    }

    @Test
    void decreaseStock_whenInsufficient_throws() {
        stockService.increase(warehouseId, productId, batchId, 10);
        assertThrows(InsufficientStockException.class,
            () -> stockService.decrease(warehouseId, productId, batchId, 20));
    }
}
