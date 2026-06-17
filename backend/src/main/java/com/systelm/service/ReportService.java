package com.systelm.service;

import com.systelm.dto.LowStockRow;
import com.systelm.dto.ProfitSummaryRow;
import com.systelm.dto.SalesSummaryRow;
import com.systelm.dto.StockSummaryRow;
import com.systelm.entity.*;
import com.systelm.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseAccessService warehouseAccessService;

    public ReportService(
        StockRepository stockRepository,
        ProductRepository productRepository,
        ProductBatchRepository productBatchRepository,
        WarehouseRepository warehouseRepository,
        SalesOrderRepository salesOrderRepository,
        SalesOrderItemRepository salesOrderItemRepository,
        CustomerRepository customerRepository,
        WarehouseAccessService warehouseAccessService
    ) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.productBatchRepository = productBatchRepository;
        this.warehouseRepository = warehouseRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.customerRepository = customerRepository;
        this.warehouseAccessService = warehouseAccessService;
    }

    public List<StockSummaryRow> stockSummary(UserAccount user, Long warehouseId) {
        Set<Long> allowed = accessibleWarehouseIds(user, warehouseId);
        if (allowed.isEmpty()) {
            return List.of();
        }

        Map<Long, Product> products = productRepository.findAll().stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, ProductBatch> batches = productBatchRepository.findAll().stream()
            .collect(Collectors.toMap(ProductBatch::getId, b -> b));
        Map<Long, String> warehouseNames = warehouseRepository.findAll().stream()
            .collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        return stockRepository.findAll().stream()
            .filter(s -> allowed.contains(s.getWarehouseId()))
            .map(s -> {
                Product product = products.get(s.getProductId());
                ProductBatch batch = batches.get(s.getBatchId());
                double cost = product != null ? product.getCostPrice() : 0.0;
                return new StockSummaryRow(
                    s.getWarehouseId(),
                    warehouseNames.getOrDefault(s.getWarehouseId(), ""),
                    s.getProductId(),
                    product != null ? product.getName() : "",
                    product != null ? product.getSku() : "",
                    s.getBatchId(),
                    batch != null ? batch.getBatchNo() : "",
                    s.getQuantity(),
                    s.getQuantity() * cost
                );
            })
            .sorted(Comparator.comparing(StockSummaryRow::warehouseName)
                .thenComparing(StockSummaryRow::productName))
            .toList();
    }

    public List<SalesSummaryRow> salesSummary(UserAccount user, String from, String to) {
        requireReportRole(user);
        List<SalesOrder> orders = salesOrderRepository.findAll().stream()
            .filter(o -> inDateRange(o.getCreatedAt(), from, to))
            .toList();

        Map<Long, Customer> customers = customerRepository.findAll().stream()
            .collect(Collectors.toMap(Customer::getId, c -> c));
        Map<Long, Product> products = productRepository.findAll().stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, List<SalesOrderItem>> itemsByOrder = salesOrderItemRepository.findAll().stream()
            .collect(Collectors.groupingBy(SalesOrderItem::getSalesOrderId));

        List<SalesSummaryRow> rows = new ArrayList<>();
        for (SalesOrder order : orders) {
            Customer customer = customers.get(order.getCustomerId());
            String period = order.getCreatedAt().substring(0, 10);
            for (SalesOrderItem item : itemsByOrder.getOrDefault(order.getId(), List.of())) {
                Product product = products.get(item.getProductId());
                rows.add(new SalesSummaryRow(
                    period,
                    item.getProductId(),
                    product != null ? product.getName() : "",
                    order.getCustomerId(),
                    customer != null ? customer.getName() : "",
                    item.getQuantity(),
                    item.getAmount()
                ));
            }
        }
        return rows;
    }

    public List<ProfitSummaryRow> profitSummary(UserAccount user, String from, String to) {
        requireReportRole(user);
        List<SalesOrder> orders = salesOrderRepository.findAll().stream()
            .filter(o -> inDateRange(o.getCreatedAt(), from, to))
            .toList();

        Map<Long, Product> products = productRepository.findAll().stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, List<SalesOrderItem>> itemsByOrder = salesOrderItemRepository.findAll().stream()
            .collect(Collectors.groupingBy(SalesOrderItem::getSalesOrderId));

        Map<String, double[]> byPeriod = new TreeMap<>();
        for (SalesOrder order : orders) {
            String period = order.getCreatedAt().substring(0, 10);
            double[] totals = byPeriod.computeIfAbsent(period, k -> new double[2]);
            for (SalesOrderItem item : itemsByOrder.getOrDefault(order.getId(), List.of())) {
                Product product = products.get(item.getProductId());
                double cost = product != null ? product.getCostPrice() : 0.0;
                totals[0] += item.getAmount();
                totals[1] += item.getQuantity() * cost;
            }
        }

        return byPeriod.entrySet().stream()
            .map(e -> new ProfitSummaryRow(
                e.getKey(),
                e.getValue()[0],
                e.getValue()[1],
                e.getValue()[0] - e.getValue()[1]
            ))
            .toList();
    }

    public List<LowStockRow> lowStock(UserAccount user, Long warehouseId) {
        Set<Long> allowed = accessibleWarehouseIds(user, warehouseId);
        if (allowed.isEmpty()) {
            return List.of();
        }

        Map<Long, Product> products = productRepository.findAll().stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, String> warehouseNames = warehouseRepository.findAll().stream()
            .collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        Map<String, Double> qtyByKey = new HashMap<>();
        for (Stock stock : stockRepository.findAll()) {
            if (!allowed.contains(stock.getWarehouseId())) {
                continue;
            }
            String key = stock.getWarehouseId() + ":" + stock.getProductId();
            qtyByKey.merge(key, stock.getQuantity(), Double::sum);
        }

        List<LowStockRow> rows = new ArrayList<>();
        for (Map.Entry<String, Double> entry : qtyByKey.entrySet()) {
            String[] parts = entry.getKey().split(":");
            Long whId = Long.parseLong(parts[0]);
            Long prodId = Long.parseLong(parts[1]);
            Product product = products.get(prodId);
            if (product == null || product.getMinStock() <= 0) {
                continue;
            }
            double qty = entry.getValue();
            if (qty < product.getMinStock()) {
                rows.add(new LowStockRow(
                    whId,
                    warehouseNames.getOrDefault(whId, ""),
                    prodId,
                    product.getName(),
                    product.getSku(),
                    qty,
                    product.getMinStock()
                ));
            }
        }
        rows.sort(Comparator.comparing(LowStockRow::warehouseName).thenComparing(LowStockRow::productName));
        return rows;
    }

    private Set<Long> accessibleWarehouseIds(UserAccount user, Long warehouseId) {
        String role = user.getRole().getName();
        Set<Long> ids = new HashSet<>();
        if ("admin".equals(role) || "finance".equals(role)) {
            warehouseRepository.findAll().forEach(w -> ids.add(w.getId()));
        } else {
            user.getWarehouses().forEach(w -> ids.add(w.getId()));
        }
        if (warehouseId != null) {
            if (!warehouseAccessService.canAccessWarehouse(user, warehouseId)) {
                return Set.of();
            }
            return Set.of(warehouseId);
        }
        return ids;
    }

    private void requireReportRole(UserAccount user) {
        String role = user.getRole().getName();
        if (!"admin".equals(role) && !"finance".equals(role)) {
            throw new IllegalArgumentException("无权查看报表");
        }
    }

    private boolean inDateRange(String createdAt, String from, String to) {
        if (from != null && !from.isBlank() && createdAt.compareTo(from) < 0) {
            return false;
        }
        if (to != null && !to.isBlank()) {
            String toInclusive = to.length() == 10 ? to + " 23:59:59" : to;
            return createdAt.compareTo(toInclusive) <= 0;
        }
        return true;
    }
}
