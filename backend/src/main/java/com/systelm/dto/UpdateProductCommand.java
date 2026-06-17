package com.systelm.dto;

public record UpdateProductCommand(
    String name,
    String sku,
    Long categoryId,
    Double costPrice,
    Double salePrice,
    String unit,
    Double minStock,
    String status
) {}
