package com.systelm.service;

import com.systelm.dto.CreateWarehouseCommand;
import com.systelm.dto.UpdateWarehouseCommand;
import com.systelm.entity.Warehouse;
import com.systelm.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public List<Warehouse> listWarehouses() {
        return warehouseRepository.findAll();
    }

    @Transactional
    public Warehouse createWarehouse(CreateWarehouseCommand cmd) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(cmd.name());
        warehouse.setAddress(cmd.address());
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(Long id, UpdateWarehouseCommand cmd) {
        Warehouse warehouse = warehouseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("仓库不存在"));
        if (cmd.name() != null) {
            warehouse.setName(cmd.name());
        }
        if (cmd.address() != null) {
            warehouse.setAddress(cmd.address());
        }
        if (cmd.status() != null) {
            warehouse.setStatus(cmd.status());
        }
        return warehouseRepository.save(warehouse);
    }
}
