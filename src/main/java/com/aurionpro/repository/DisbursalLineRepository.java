package com.aurionpro.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aurionpro.entity.DisbursalLine;

@Repository
public interface DisbursalLineRepository extends JpaRepository<DisbursalLine, Long> {
    List<DisbursalLine> findByBatchId(Long batchId);
}

