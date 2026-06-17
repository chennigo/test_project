package com.systelm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "cost_price", nullable = false)
    private Double costPrice = 0.0;

    @Column(name = "sale_price", nullable = false)
    private Double salePrice = 0.0;

    @Column(nullable = false)
    private String unit = "件";

    @Column(name = "min_stock", nullable = false)
    private Double minStock = 0.0;

    @Column(nullable = false)
    private String status = "ACTIVE";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }
    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getMinStock() { return minStock; }
    public void setMinStock(Double minStock) { this.minStock = minStock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
