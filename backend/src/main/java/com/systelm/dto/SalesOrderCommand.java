package com.systelm.dto;

import java.util.List;

public record SalesOrderCommand(
    Long customerId,
    Long warehouseId,
    List<SalesOrderItemCommand> items
) {}
