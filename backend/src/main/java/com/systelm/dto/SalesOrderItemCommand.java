package com.systelm.dto;

public record SalesOrderItemCommand(
    Long productId,
    Long batchId,
    double quantity,
    double unitPrice
) {}
