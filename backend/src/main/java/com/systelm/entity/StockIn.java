package com.systelm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_in")
public class StockIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doc_no", nullable = false, unique = true)
    private String docNo;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
