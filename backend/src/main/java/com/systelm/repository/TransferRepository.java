package com.systelm.repository;

import com.systelm.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    long countByDocNoStartingWith(String prefix);
}
