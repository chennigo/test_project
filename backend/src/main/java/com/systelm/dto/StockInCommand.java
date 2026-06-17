package com.systelm.dto;

import java.util.List;

public record StockInCommand(
    Long warehouseId,
    List<StockInItemCommand> items,
    String remark
) {
    public StockInCommand(Long warehouseId, List<StockInItemCommand> items) {
        this(warehouseId, items, null);
    }
}
