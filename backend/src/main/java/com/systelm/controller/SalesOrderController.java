package com.systelm.controller;

import com.systelm.dto.SalesOrderCommand;
import com.systelm.entity.SalesOrder;
import com.systelm.service.CurrentUserService;
import com.systelm.service.SalesOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final CurrentUserService currentUserService;

    public SalesOrderController(SalesOrderService salesOrderService, CurrentUserService currentUserService) {
        this.salesOrderService = salesOrderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<SalesOrder> listSalesOrders() {
        return salesOrderService.listSalesOrders();
    }

    @PostMapping
    public SalesOrder create(@RequestBody SalesOrderCommand cmd) {
        return salesOrderService.confirm(currentUserService.requireCurrentUser(), cmd);
    }
}
