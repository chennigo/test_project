package com.systelm.service;

import com.systelm.dto.CreateBatchCommand;
import com.systelm.dto.CreateProductCommand;
import com.systelm.dto.TransferCommand;
import com.systelm.dto.TransferItemCommand;
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
class TransferServiceTest {

    @Autowired TransferService transferService;
    @Autowired StockService stockService;
    @Autowired ProductService productService;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired UserAccountRepository userAccountRepository;

    private UserAccount operator;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Long productId;
    private Long batchId;

    @BeforeEach
    void setUp() {
        operator = userAccountRepository.findByUsername("admin").orElseThrow();

        Warehouse fromWarehouse = new Warehouse();
        fromWarehouse.setName("调出仓-" + UUID.randomUUID().toString().substring(0, 6));
        fromWarehouse.setStatus("ACTIVE");
        fromWarehouseId = warehouseRepository.save(fromWarehouse).getId();

        Warehouse toWarehouse = new Warehouse();
        toWarehouse.setName("调入仓-" + UUID.randomUUID().toString().substring(0, 6));
        toWarehouse.setStatus("ACTIVE");
        toWarehouseId = warehouseRepository.save(toWarehouse).getId();

        Product product = productService.createProduct(
            new CreateProductCommand("调拨商品", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                1.0, 2.0, "件", 0.0));
        productId = product.getId();

        ProductBatch batch = productService.createBatch(
            new CreateBatchCommand(productId, "B-" + UUID.randomUUID().toString().substring(0, 6), "2026-01-01"));
        batchId = batch.getId();
    }

    @Test
    void confirmTransfer_movesStockBetweenWarehouses() {
        stockService.increase(fromWarehouseId, productId, batchId, 50);
        transferService.confirm(operator, new TransferCommand(
            fromWarehouseId, toWarehouseId,
            List.of(new TransferItemCommand(productId, batchId, 20))));
        assertEquals(30, stockService.getQuantity(fromWarehouseId, productId, batchId));
        assertEquals(20, stockService.getQuantity(toWarehouseId, productId, batchId));
    }
}
