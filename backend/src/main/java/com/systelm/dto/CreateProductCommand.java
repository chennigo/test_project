package com.systelm.dto;

public record CreateProductCommand(
    String name,
    String sku,
    Double costPrice,
    Double salePrice,
    String unit,
    Double minStock
) {}
