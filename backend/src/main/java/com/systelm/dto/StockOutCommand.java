package com.systelm.dto;

import java.util.List;

public record StockOutCommand(
    Long warehouseId,
    String outType,
    List<StockOutItemCommand> items,
    String remark
) {
    public StockOutCommand(Long warehouseId, String outType, List<StockOutItemCommand> items) {
        this(warehouseId, outType, items, null);
    }
}
