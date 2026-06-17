package com.systelm.controller;

import com.systelm.dto.StockCheckCommand;
import com.systelm.entity.StockCheck;
import com.systelm.service.CurrentUserService;
import com.systelm.service.StockCheckService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-checks")
public class StockCheckController {

    private final StockCheckService stockCheckService;
    private final CurrentUserService currentUserService;

    public StockCheckController(StockCheckService stockCheckService, CurrentUserService currentUserService) {
        this.stockCheckService = stockCheckService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public StockCheck create(@RequestBody StockCheckCommand cmd) {
        return stockCheckService.confirm(currentUserService.requireCurrentUser(), cmd);
    }
}
