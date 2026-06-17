package com.systelm.controller;

import com.systelm.dto.StockInCommand;
import com.systelm.entity.StockIn;
import com.systelm.service.CurrentUserService;
import com.systelm.service.StockInService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-ins")
public class StockInController {

    private final StockInService stockInService;
    private final CurrentUserService currentUserService;

    public StockInController(StockInService stockInService, CurrentUserService currentUserService) {
        this.stockInService = stockInService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public StockIn create(@RequestBody StockInCommand cmd) {
        return stockInService.confirm(currentUserService.requireCurrentUser(), cmd);
    }
}
