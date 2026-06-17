package com.systelm.service;

import com.systelm.dto.SalesOrderCommand;
import com.systelm.dto.SalesOrderItemCommand;
import com.systelm.dto.StockOutCommand;
import com.systelm.dto.StockOutItemCommand;
import com.systelm.entity.SalesOrder;
import com.systelm.entity.SalesOrderItem;
import com.systelm.entity.UserAccount;
import com.systelm.repository.CustomerRepository;
import com.systelm.repository.SalesOrderItemRepository;
import com.systelm.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SalesOrderService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final CustomerRepository customerRepository;
    private final StockOutService stockOutService;
    private final WarehouseAccessService warehouseAccessService;
    private final DocNoGenerator docNoGenerator;

    public SalesOrderService(
            SalesOrderRepository salesOrderRepository,
            SalesOrderItemRepository salesOrderItemRepository,
            CustomerRepository customerRepository,
            StockOutService stockOutService,
            WarehouseAccessService warehouseAccessService,
            DocNoGenerator docNoGenerator) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.customerRepository = customerRepository;
        this.stockOutService = stockOutService;
        this.warehouseAccessService = warehouseAccessService;
        this.docNoGenerator = docNoGenerator;
    }

    public List<SalesOrder> listSalesOrders() {
        return salesOrderRepository.findAll();
    }

    @Transactional
    public SalesOrder confirm(UserAccount operator, SalesOrderCommand cmd) {
        warehouseAccessService.requireWarehouseAccess(operator, cmd.warehouseId());
        customerRepository.findById(cmd.customerId())
            .orElseThrow(() -> new IllegalArgumentException("客户不存在"));

        double totalAmount = cmd.items().stream()
            .mapToDouble(item -> item.quantity() * item.unitPrice())
            .sum();

        String docPrefix = "SO" + LocalDate.now().format(DATE_FORMAT);
        String docNo = docNoGenerator.next("SO", () -> salesOrderRepository.countByDocNoStartingWith(docPrefix));

        SalesOrder order = new SalesOrder();
        order.setDocNo(docNo);
        order.setCustomerId(cmd.customerId());
        order.setWarehouseId(cmd.warehouseId());
        order.setOperatorId(operator.getId());
        order.setTotalAmount(totalAmount);
        order.setStatus("COMPLETED");
        order.setCreatedAt(LocalDateTime.now().format(DATE_TIME_FORMAT));
        order = salesOrderRepository.save(order);

        for (SalesOrderItemCommand item : cmd.items()) {
            SalesOrderItem orderItem = new SalesOrderItem();
            orderItem.setSalesOrderId(order.getId());
            orderItem.setProductId(item.productId());
            orderItem.setBatchId(item.batchId());
            orderItem.setQuantity(item.quantity());
            orderItem.setUnitPrice(item.unitPrice());
            orderItem.setAmount(item.quantity() * item.unitPrice());
            salesOrderItemRepository.save(orderItem);
        }

        List<StockOutItemCommand> outItems = cmd.items().stream()
            .map(item -> new StockOutItemCommand(item.productId(), item.batchId(), item.quantity()))
            .toList();
        stockOutService.confirm(operator, new StockOutCommand(cmd.warehouseId(), "SALES", outItems));

        return order;
    }
}
