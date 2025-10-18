package com.aurionpro.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.PaymentBatch;

@Repository
public interface PaymentBatchRepository extends JpaRepository<PaymentBatch, Long> {
	Page<PaymentBatch> findByStatus(PaymentBatch.Status status, Pageable pageable);
	Page<PaymentBatch> findByStatusAndType(PaymentBatch.Status status, PaymentBatch.Type type, Pageable pageable);


    List<PaymentBatch> findByOrganizationIdAndTypeAndPaymentDate(Long orgId, PaymentBatch.Type type, LocalDate date);
    boolean existsByOrganizationIdAndTypeAndPaymentDate(Long orgId, PaymentBatch.Type type, LocalDate paymentDate);

}

