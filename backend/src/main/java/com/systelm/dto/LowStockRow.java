package com.systelm.dto;

public record LowStockRow(
    Long warehouseId,
    String warehouseName,
    Long productId,
    String productName,
    String sku,
    double currentQty,
    double minStock
) {}
