package com.systelm.service;

import com.systelm.dto.CreateBatchCommand;
import com.systelm.dto.CreateCustomerCommand;
import com.systelm.dto.CreateProductCommand;
import com.systelm.dto.SalesOrderCommand;
import com.systelm.dto.SalesOrderItemCommand;
import com.systelm.entity.Customer;
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
class SalesOrderServiceTest {

    @Autowired SalesOrderService salesOrderService;
    @Autowired CustomerService customerService;
    @Autowired StockService stockService;
    @Autowired ProductService productService;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired UserAccountRepository userAccountRepository;

    private UserAccount operator;
    private Long warehouseId;
    private Long productId;
    private Long batchId;
    private Long customerId;

    @BeforeEach
    void setUp() {
        operator = userAccountRepository.findByUsername("admin").orElseThrow();

        Warehouse warehouse = new Warehouse();
        warehouse.setName("销售测试仓-" + UUID.randomUUID().toString().substring(0, 6));
        warehouse.setStatus("ACTIVE");
        warehouseId = warehouseRepository.save(warehouse).getId();

        Product product = productService.createProduct(
            new CreateProductCommand("销售商品", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                1.0, 2.0, "件", 0.0));
        productId = product.getId();

        ProductBatch batch = productService.createBatch(
            new CreateBatchCommand(productId, "B-" + UUID.randomUUID().toString().substring(0, 6), "2026-01-01"));
        batchId = batch.getId();

        Customer customer = customerService.createCustomer(
            new CreateCustomerCommand("测试客户", "张三", "13800000000", "测试地址"));
        customerId = customer.getId();
    }

    @Test
    void confirmSalesOrder_decreasesStockAndCalculatesTotal() {
        stockService.increase(warehouseId, productId, batchId, 100);
        var order = salesOrderService.confirm(operator, new SalesOrderCommand(
            customerId, warehouseId,
            List.of(new SalesOrderItemCommand(productId, batchId, 5, 10.0))));
        assertEquals(50.0, order.getTotalAmount());
        assertEquals(95, stockService.getQuantity(warehouseId, productId, batchId));
    }
}
