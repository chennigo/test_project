package com.systelm.dto;

public record StockSummaryRow(
    Long warehouseId,
    String warehouseName,
    Long productId,
    String productName,
    String sku,
    Long batchId,
    String batchNo,
    double quantity,
    double totalValue
) {}
