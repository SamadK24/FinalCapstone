package com.aurionpro.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aurionpro.entity.PaymentLine;

@Repository
public interface PaymentLineRepository extends JpaRepository<PaymentLine, Long> {
    List<PaymentLine> findByBatchId(Long batchId);
}

