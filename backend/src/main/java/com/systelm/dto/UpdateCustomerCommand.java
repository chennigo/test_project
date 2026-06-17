package com.systelm.dto;

public record UpdateCustomerCommand(
    String name,
    String contact,
    String phone,
    String address
) {}
