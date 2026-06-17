package com.systelm.service;

import com.systelm.dto.StockInCommand;
import com.systelm.dto.StockInItemCommand;
import com.systelm.entity.StockIn;
import com.systelm.entity.StockInItem;
import com.systelm.entity.UserAccount;
import com.systelm.repository.StockInItemRepository;
import com.systelm.repository.StockInRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StockInService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final StockInRepository stockInRepository;
    private final StockInItemRepository stockInItemRepository;
    private final StockService stockService;
    private final WarehouseAccessService warehouseAccessService;
    private final DocNoGenerator docNoGenerator;

    public StockInService(
            StockInRepository stockInRepository,
            StockInItemRepository stockInItemRepository,
            StockService stockService,
            WarehouseAccessService warehouseAccessService,
            DocNoGenerator docNoGenerator) {
        this.stockInRepository = stockInRepository;
        this.stockInItemRepository = stockInItemRepository;
        this.stockService = stockService;
        this.warehouseAccessService = warehouseAccessService;
        this.docNoGenerator = docNoGenerator;
    }

    @Transactional
    public StockIn confirm(UserAccount operator, StockInCommand cmd) {
        warehouseAccessService.requireWarehouseAccess(operator, cmd.warehouseId());

        String docPrefix = "IN" + LocalDate.now().format(DATE_FORMAT);
        String docNo = docNoGenerator.next("IN", () -> stockInRepository.countByDocNoStartingWith(docPrefix));

        StockIn stockIn = new StockIn();
        stockIn.setDocNo(docNo);
        stockIn.setWarehouseId(cmd.warehouseId());
        stockIn.setOperatorId(operator.getId());
        stockIn.setRemark(cmd.remark());
        stockIn = stockInRepository.save(stockIn);

        for (StockInItemCommand item : cmd.items()) {
            StockInItem stockInItem = new StockInItem();
            stockInItem.setStockInId(stockIn.getId());
            stockInItem.setProductId(item.productId());
            stockInItem.setBatchId(item.batchId());
            stockInItem.setQuantity(item.quantity());
            stockInItemRepository.save(stockInItem);

            stockService.increase(cmd.warehouseId(), item.productId(), item.batchId(), item.quantity());
        }

        return stockIn;
    }
}
