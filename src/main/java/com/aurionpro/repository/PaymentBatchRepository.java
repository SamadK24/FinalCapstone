package com.aurionpro.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aurionpro.entity.PaymentBatch;

@Repository
public interface PaymentBatchRepository extends JpaRepository<PaymentBatch, Long> {
    List<PaymentBatch> findByStatus(PaymentBatch.Status status);
    List<PaymentBatch> findByOrganizationIdAndTypeAndPaymentDate(Long orgId, PaymentBatch.Type type, LocalDate date);
}

