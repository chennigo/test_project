package com.systelm.controller;

import com.systelm.dto.CreateWarehouseCommand;
import com.systelm.dto.UpdateWarehouseCommand;
import com.systelm.entity.Warehouse;
import com.systelm.service.WarehouseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public List<Warehouse> listWarehouses() {
        return warehouseService.listWarehouses();
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Warehouse createWarehouse(@RequestBody CreateWarehouseCommand cmd) {
        return warehouseService.createWarehouse(cmd);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Warehouse updateWarehouse(@PathVariable Long id, @RequestBody UpdateWarehouseCommand cmd) {
        return warehouseService.updateWarehouse(id, cmd);
    }
}
