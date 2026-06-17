package com.systelm.service;

import com.systelm.dto.StockCheckCommand;
import com.systelm.dto.StockCheckItemCommand;
import com.systelm.entity.StockCheck;
import com.systelm.entity.StockCheckItem;
import com.systelm.entity.UserAccount;
import com.systelm.repository.StockCheckItemRepository;
import com.systelm.repository.StockCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StockCheckService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final StockCheckRepository stockCheckRepository;
    private final StockCheckItemRepository stockCheckItemRepository;
    private final StockService stockService;
    private final WarehouseAccessService warehouseAccessService;
    private final DocNoGenerator docNoGenerator;

    public StockCheckService(
            StockCheckRepository stockCheckRepository,
            StockCheckItemRepository stockCheckItemRepository,
            StockService stockService,
            WarehouseAccessService warehouseAccessService,
            DocNoGenerator docNoGenerator) {
        this.stockCheckRepository = stockCheckRepository;
        this.stockCheckItemRepository = stockCheckItemRepository;
        this.stockService = stockService;
        this.warehouseAccessService = warehouseAccessService;
        this.docNoGenerator = docNoGenerator;
    }

    @Transactional
    public StockCheck confirm(UserAccount operator, StockCheckCommand cmd) {
        warehouseAccessService.requireWarehouseAccess(operator, cmd.warehouseId());

        String docPrefix = "CK" + LocalDate.now().format(DATE_FORMAT);
        String docNo = docNoGenerator.next("CK", () -> stockCheckRepository.countByDocNoStartingWith(docPrefix));

        StockCheck stockCheck = new StockCheck();
        stockCheck.setDocNo(docNo);
        stockCheck.setWarehouseId(cmd.warehouseId());
        stockCheck.setOperatorId(operator.getId());
        stockCheck.setRemark(cmd.remark());
        stockCheck = stockCheckRepository.save(stockCheck);

        for (StockCheckItemCommand item : cmd.items()) {
            double bookQty = stockService.getQuantity(cmd.warehouseId(), item.productId(), item.batchId());
            double diffQty = item.actualQty() - bookQty;

            StockCheckItem checkItem = new StockCheckItem();
            checkItem.setStockCheckId(stockCheck.getId());
            checkItem.setProductId(item.productId());
            checkItem.setBatchId(item.batchId());
            checkItem.setBookQty(bookQty);
            checkItem.setActualQty(item.actualQty());
            checkItem.setDiffQty(diffQty);
            stockCheckItemRepository.save(checkItem);

            if (diffQty > 0) {
                stockService.increase(cmd.warehouseId(), item.productId(), item.batchId(), diffQty);
            } else if (diffQty < 0) {
                stockService.decrease(cmd.warehouseId(), item.productId(), item.batchId(), -diffQty);
            }
        }

        return stockCheck;
    }
}
