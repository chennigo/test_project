package com.systelm.controller;

import com.systelm.dto.LowStockRow;
import com.systelm.dto.ProfitSummaryRow;
import com.systelm.dto.SalesSummaryRow;
import com.systelm.dto.StockSummaryRow;
import com.systelm.service.CurrentUserService;
import com.systelm.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final CurrentUserService currentUserService;

    public ReportController(ReportService reportService, CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/stock")
    @PreAuthorize("hasAnyRole('admin', 'finance')")
    public List<StockSummaryRow> stock(@RequestParam(required = false) Long warehouseId) {
        return reportService.stockSummary(currentUserService.requireCurrentUser(), warehouseId);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('admin', 'finance')")
    public List<SalesSummaryRow> sales(
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to
    ) {
        return reportService.salesSummary(currentUserService.requireCurrentUser(), from, to);
    }

    @GetMapping("/profit")
    @PreAuthorize("hasAnyRole('admin', 'finance')")
    public List<ProfitSummaryRow> profit(
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to
    ) {
        return reportService.profitSummary(currentUserService.requireCurrentUser(), from, to);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('admin', 'warehouse_sales', 'finance')")
    public List<LowStockRow> lowStock(@RequestParam(required = false) Long warehouseId) {
        return reportService.lowStock(currentUserService.requireCurrentUser(), warehouseId);
    }
}
