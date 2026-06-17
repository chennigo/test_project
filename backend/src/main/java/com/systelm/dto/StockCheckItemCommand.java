package com.systelm.dto;

public record StockCheckItemCommand(
    Long productId,
    Long batchId,
    Double actualQty
) {}
