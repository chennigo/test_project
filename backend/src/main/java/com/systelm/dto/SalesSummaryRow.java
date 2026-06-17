package com.systelm.dto;

public record SalesSummaryRow(
    String period,
    Long productId,
    String productName,
    Long customerId,
    String customerName,
    double quantity,
    double amount
) {}
