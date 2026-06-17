package com.systelm.service;

import com.systelm.entity.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class WarehouseAccessService {

    public boolean canAccessWarehouse(UserAccount user, Long warehouseId) {
        if ("admin".equals(user.getRole().getName())) {
            return true;
        }
        return user.getWarehouses().stream()
            .anyMatch(w -> w.getId().equals(warehouseId));
    }

    public void requireWarehouseAccess(UserAccount user, Long warehouseId) {
        if (!canAccessWarehouse(user, warehouseId)) {
            throw new IllegalArgumentException("无权操作该仓库");
        }
    }
}
