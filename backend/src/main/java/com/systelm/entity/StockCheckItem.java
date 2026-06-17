package com.systelm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_check_item")
public class StockCheckItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_check_id", nullable = false)
    private Long stockCheckId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "book_qty", nullable = false)
    private Double bookQty;

    @Column(name = "actual_qty", nullable = false)
    private Double actualQty;

    @Column(name = "diff_qty", nullable = false)
    private Double diffQty;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStockCheckId() { return stockCheckId; }
    public void setStockCheckId(Long stockCheckId) { this.stockCheckId = stockCheckId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Double getBookQty() { return bookQty; }
    public void setBookQty(Double bookQty) { this.bookQty = bookQty; }
    public Double getActualQty() { return actualQty; }
    public void setActualQty(Double actualQty) { this.actualQty = actualQty; }
    public Double getDiffQty() { return diffQty; }
    public void setDiffQty(Double diffQty) { this.diffQty = diffQty; }
}
