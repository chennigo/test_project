package com.systelm.dto;

public record TransferItemCommand(
    Long productId,
    Long batchId,
    double quantity
) {}
