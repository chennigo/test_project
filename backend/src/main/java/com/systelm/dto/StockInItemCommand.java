package com.systelm.dto;

public record StockInItemCommand(
    Long productId,
    Long batchId,
    Double quantity
) {}
