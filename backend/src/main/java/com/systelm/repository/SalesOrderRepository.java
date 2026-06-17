package com.systelm.repository;

import com.systelm.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    long countByDocNoStartingWith(String prefix);

    List<SalesOrder> findByCustomerIdOrderByIdDesc(Long customerId);
}
