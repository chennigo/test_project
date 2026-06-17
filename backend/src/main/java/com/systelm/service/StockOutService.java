package com.systelm.service;

import com.systelm.dto.StockOutCommand;
import com.systelm.dto.StockOutItemCommand;
import com.systelm.entity.StockOut;
import com.systelm.entity.StockOutItem;
import com.systelm.entity.UserAccount;
import com.systelm.repository.StockOutItemRepository;
import com.systelm.repository.StockOutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StockOutService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final StockOutRepository stockOutRepository;
    private final StockOutItemRepository stockOutItemRepository;
    private final StockService stockService;
    private final WarehouseAccessService warehouseAccessService;
    private final DocNoGenerator docNoGenerator;

    public StockOutService(
            StockOutRepository stockOutRepository,
            StockOutItemRepository stockOutItemRepository,
            StockService stockService,
            WarehouseAccessService warehouseAccessService,
            DocNoGenerator docNoGenerator) {
        this.stockOutRepository = stockOutRepository;
        this.stockOutItemRepository = stockOutItemRepository;
        this.stockService = stockService;
        this.warehouseAccessService = warehouseAccessService;
        this.docNoGenerator = docNoGenerator;
    }

    @Transactional
    public StockOut confirm(UserAccount operator, StockOutCommand cmd) {
        warehouseAccessService.requireWarehouseAccess(operator, cmd.warehouseId());

        String docPrefix = "OUT" + LocalDate.now().format(DATE_FORMAT);
        String docNo = docNoGenerator.next("OUT", () -> stockOutRepository.countByDocNoStartingWith(docPrefix));

        StockOut stockOut = new StockOut();
        stockOut.setDocNo(docNo);
        stockOut.setWarehouseId(cmd.warehouseId());
        stockOut.setOperatorId(operator.getId());
        stockOut.setOutType(cmd.outType());
        stockOut.setRemark(cmd.remark());
        stockOut = stockOutRepository.save(stockOut);

        for (StockOutItemCommand item : cmd.items()) {
            StockOutItem stockOutItem = new StockOutItem();
            stockOutItem.setStockOutId(stockOut.getId());
            stockOutItem.setProductId(item.productId());
            stockOutItem.setBatchId(item.batchId());
            stockOutItem.setQuantity(item.quantity());
            stockOutItemRepository.save(stockOutItem);

            stockService.decrease(cmd.warehouseId(), item.productId(), item.batchId(), item.quantity());
        }

        return stockOut;
    }
}
