package com.systelm.service;

import com.systelm.entity.Role;
import com.systelm.entity.UserAccount;
import com.systelm.entity.Warehouse;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarehouseAccessServiceTest {

    private final WarehouseAccessService service = new WarehouseAccessService();

    @Test
    void admin_canAccessAnyWarehouse() {
        UserAccount admin = userWithRole("admin", Set.of());
        assertTrue(service.canAccessWarehouse(admin, 99L));
    }

    @Test
    void warehouseSales_canOnlyAccessAssignedWarehouse() {
        Warehouse w1 = warehouse(1L);
        UserAccount user = userWithRole("warehouse_sales", Set.of(w1));
        assertTrue(service.canAccessWarehouse(user, 1L));
        assertFalse(service.canAccessWarehouse(user, 2L));
    }

    private UserAccount userWithRole(String roleName, Set<Warehouse> warehouses) {
        Role role = new Role();
        role.setName(roleName);
        UserAccount user = new UserAccount();
        user.setRole(role);
        user.setWarehouses(new HashSet<>(warehouses));
        return user;
    }

    private Warehouse warehouse(Long id) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        return warehouse;
    }
}
