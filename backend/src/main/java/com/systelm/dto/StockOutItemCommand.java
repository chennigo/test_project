package com.systelm.dto;

public record StockOutItemCommand(
    Long productId,
    Long batchId,
    Double quantity
) {}
