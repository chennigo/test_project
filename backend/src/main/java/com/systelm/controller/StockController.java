package com.systelm.controller;

import com.systelm.entity.Stock;
import com.systelm.entity.UserAccount;
import com.systelm.repository.StockRepository;
import com.systelm.service.CurrentUserService;
import com.systelm.service.WarehouseAccessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockRepository stockRepository;
    private final CurrentUserService currentUserService;
    private final WarehouseAccessService warehouseAccessService;

    public StockController(
            StockRepository stockRepository,
            CurrentUserService currentUserService,
            WarehouseAccessService warehouseAccessService) {
        this.stockRepository = stockRepository;
        this.currentUserService = currentUserService;
        this.warehouseAccessService = warehouseAccessService;
    }

    @GetMapping
    public List<Stock> listStocks(@RequestParam Long warehouseId, @RequestParam(required = false) Long productId) {
        UserAccount user = currentUserService.requireCurrentUser();
        warehouseAccessService.requireWarehouseAccess(user, warehouseId);
        if (productId != null) {
            return stockRepository.findByWarehouseIdAndProductId(warehouseId, productId);
        }
        return stockRepository.findByWarehouseId(warehouseId);
    }
}
