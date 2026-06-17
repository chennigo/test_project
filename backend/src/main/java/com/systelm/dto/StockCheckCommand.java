package com.systelm.dto;

import java.util.List;

public record StockCheckCommand(
    Long warehouseId,
    List<StockCheckItemCommand> items,
    String remark
) {
    public StockCheckCommand(Long warehouseId, List<StockCheckItemCommand> items) {
        this(warehouseId, items, null);
    }
}
