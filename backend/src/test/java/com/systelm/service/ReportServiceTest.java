package com.systelm.service;

import com.systelm.dto.CreateBatchCommand;
import com.systelm.dto.CreateProductCommand;
import com.systelm.dto.StockInCommand;
import com.systelm.dto.StockInItemCommand;
import com.systelm.dto.StockSummaryRow;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportServiceTest {

    @Autowired ReportService reportService;
    @Autowired StockInService stockInService;
    @Autowired ProductService productService;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired UserAccountRepository userAccountRepository;

    private UserAccount admin;
    private Long warehouseId;
    private Long productId;
    private Long batchId;

    @BeforeEach
    void setUp() {
        admin = userAccountRepository.findByUsername("admin").orElseThrow();

        Warehouse warehouse = new Warehouse();
        warehouse.setName("报表仓-" + UUID.randomUUID().toString().substring(0, 6));
        warehouse.setStatus("ACTIVE");
        warehouseId = warehouseRepository.save(warehouse).getId();

        Product product = productService.createProduct(
            new CreateProductCommand("报表商品", "SKU-R-" + UUID.randomUUID().toString().substring(0, 6),
                5.0, 10.0, "件", 0.0));
        productId = product.getId();

        ProductBatch batch = productService.createBatch(
            new CreateBatchCommand(productId, "BR-" + UUID.randomUUID().toString().substring(0, 6), "2026-01-01"));
        batchId = batch.getId();

        stockInService.confirm(admin, new StockInCommand(warehouseId, List.of(
            new StockInItemCommand(productId, batchId, 20.0))));
    }

    @Test
    void stockSummary_returnsQuantityAndValue() {
        List<StockSummaryRow> rows = reportService.stockSummary(admin, warehouseId);
        assertFalse(rows.isEmpty());
        StockSummaryRow row = rows.stream()
            .filter(r -> r.productId().equals(productId))
            .findFirst()
            .orElseThrow();
        assertEquals(20.0, row.quantity());
        assertEquals(100.0, row.totalValue());
    }
}
