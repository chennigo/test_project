package com.systelm.dto;

public record CreateBatchCommand(
    Long productId,
    String batchNo,
    String productionDate
) {}
