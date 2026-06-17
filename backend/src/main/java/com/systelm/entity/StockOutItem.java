package com.systelm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_out_item")
public class StockOutItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_out_id", nullable = false)
    private Long stockOutId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private Double quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStockOutId() { return stockOutId; }
    public void setStockOutId(Long stockOutId) { this.stockOutId = stockOutId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
