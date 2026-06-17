package com.systelm.controller;

import com.systelm.dto.StockOutCommand;
import com.systelm.entity.StockOut;
import com.systelm.service.CurrentUserService;
import com.systelm.service.StockOutService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-outs")
public class StockOutController {

    private final StockOutService stockOutService;
    private final CurrentUserService currentUserService;

    public StockOutController(StockOutService stockOutService, CurrentUserService currentUserService) {
        this.stockOutService = stockOutService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public StockOut create(@RequestBody StockOutCommand cmd) {
        return stockOutService.confirm(currentUserService.requireCurrentUser(), cmd);
    }
}
