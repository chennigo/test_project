package com.systelm.dto;

public record UpdateWarehouseCommand(
    String name,
    String address,
    String status
) {}
