package com.systelm.service;

import com.systelm.entity.Stock;
import com.systelm.exception.InsufficientStockException;
import com.systelm.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public double getQuantity(Long warehouseId, Long productId, Long batchId) {
        return stockRepository
            .findByWarehouseIdAndProductIdAndBatchId(warehouseId, productId, batchId)
            .map(Stock::getQuantity)
            .orElse(0.0);
    }

    @Transactional
    public void increase(Long warehouseId, Long productId, Long batchId, double qty) {
        Stock stock = stockRepository
            .findByWarehouseIdAndProductIdAndBatchId(warehouseId, productId, batchId)
            .orElseGet(() -> newStock(warehouseId, productId, batchId));
        stock.setQuantity(stock.getQuantity() + qty);
        stockRepository.save(stock);
    }

    @Transactional
    public void decrease(Long warehouseId, Long productId, Long batchId, double qty) {
        Stock stock = stockRepository
            .findByWarehouseIdAndProductIdAndBatchId(warehouseId, productId, batchId)
            .orElseThrow(() -> new InsufficientStockException("库存不存在"));
        if (stock.getQuantity() < qty) {
            throw new InsufficientStockException("库存不足");
        }
        stock.setQuantity(stock.getQuantity() - qty);
        stockRepository.save(stock);
    }

    private Stock newStock(Long warehouseId, Long productId, Long batchId) {
        Stock stock = new Stock();
        stock.setWarehouseId(warehouseId);
        stock.setProductId(productId);
        stock.setBatchId(batchId);
        stock.setQuantity(0.0);
        return stock;
    }
}
