package com.systelm.service;

import com.systelm.dto.TransferCommand;
import com.systelm.dto.TransferItemCommand;
import com.systelm.entity.Transfer;
import com.systelm.entity.TransferItem;
import com.systelm.entity.UserAccount;
import com.systelm.repository.TransferItemRepository;
import com.systelm.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TransferService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TransferRepository transferRepository;
    private final TransferItemRepository transferItemRepository;
    private final StockService stockService;
    private final WarehouseAccessService warehouseAccessService;
    private final DocNoGenerator docNoGenerator;

    public TransferService(
            TransferRepository transferRepository,
            TransferItemRepository transferItemRepository,
            StockService stockService,
            WarehouseAccessService warehouseAccessService,
            DocNoGenerator docNoGenerator) {
        this.transferRepository = transferRepository;
        this.transferItemRepository = transferItemRepository;
        this.stockService = stockService;
        this.warehouseAccessService = warehouseAccessService;
        this.docNoGenerator = docNoGenerator;
    }

    @Transactional
    public Transfer confirm(UserAccount operator, TransferCommand cmd) {
        warehouseAccessService.requireWarehouseAccess(operator, cmd.fromWarehouseId());
        warehouseAccessService.requireWarehouseAccess(operator, cmd.toWarehouseId());

        String docPrefix = "TRF" + LocalDate.now().format(DATE_FORMAT);
        String docNo = docNoGenerator.next("TRF", () -> transferRepository.countByDocNoStartingWith(docPrefix));

        Transfer transfer = new Transfer();
        transfer.setDocNo(docNo);
        transfer.setFromWarehouseId(cmd.fromWarehouseId());
        transfer.setToWarehouseId(cmd.toWarehouseId());
        transfer.setOperatorId(operator.getId());
        transfer.setStatus("COMPLETED");
        transfer.setCreatedAt(LocalDateTime.now().format(DATE_TIME_FORMAT));
        transfer = transferRepository.save(transfer);

        for (TransferItemCommand item : cmd.items()) {
            TransferItem transferItem = new TransferItem();
            transferItem.setTransferId(transfer.getId());
            transferItem.setProductId(item.productId());
            transferItem.setBatchId(item.batchId());
            transferItem.setQuantity(item.quantity());
            transferItemRepository.save(transferItem);

            stockService.decrease(cmd.fromWarehouseId(), item.productId(), item.batchId(), item.quantity());
            stockService.increase(cmd.toWarehouseId(), item.productId(), item.batchId(), item.quantity());
        }

        return transfer;
    }
}
