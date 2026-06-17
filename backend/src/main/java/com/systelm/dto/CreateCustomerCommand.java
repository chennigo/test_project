package com.systelm.dto;

public record CreateCustomerCommand(
    String name,
    String contact,
    String phone,
    String address
) {}
