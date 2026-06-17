package com.systelm.dto;

import java.util.List;

public record TransferCommand(
    Long fromWarehouseId,
    Long toWarehouseId,
    List<TransferItemCommand> items
) {}
