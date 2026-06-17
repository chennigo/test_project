package com.systelm.service;

import com.systelm.dto.CreateBatchCommand;
import com.systelm.dto.CreateProductCommand;
import com.systelm.dto.StockCheckCommand;
import com.systelm.dto.StockCheckItemCommand;
import com.systelm.entity.Product;
import com.systelm.entity.ProductBatch;
import com.systelm.entity.UserAccount;
import com.systelm.entity.Warehouse;
import com.systelm.repository.UserAccountRepository;
import com.systelm.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class StockCheckServiceTest {

    @Autowired StockCheckService stockCheckService;
    @Autowired StockService stockService;
    @Autowired ProductService productService;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired UserAccountRepository userAccountRepository;

    private UserAccount operator;
    private Long warehouseId;
    private Long productId;
    private Long batchId;

    @BeforeEach
    void setUp() {
        operator = userAccountRepository.findByUsername("admin").orElseThrow();

        Warehouse warehouse = new Warehouse();
        warehouse.setName("盘点测试仓-" + UUID.randomUUID().toString().substring(0, 6));
        warehouse.setStatus("ACTIVE");
        warehouseId = warehouseRepository.save(warehouse).getId();

        Product product = productService.createProduct(
            new CreateProductCommand("盘点商品", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                1.0, 2.0, "件", 0.0));
        productId = product.getId();

        ProductBatch batch = productService.createBatch(
            new CreateBatchCommand(productId, "B-" + UUID.randomUUID().toString().substring(0, 6), "2026-01-01"));
        batchId = batch.getId();
    }

    @Test
    void confirmStockCheck_adjustsStockByDiff() {
        stockService.increase(warehouseId, productId, batchId, 10);
        var cmd = new StockCheckCommand(warehouseId, List.of(
            new StockCheckItemCommand(productId, batchId, 8.0)));
        stockCheckService.confirm(operator, cmd);
        assertEquals(8, stockService.getQuantity(warehouseId, productId, batchId));
    }
}
