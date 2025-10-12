package com.aurionpro.repository;

import com.aurionpro.entity.ConcernAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConcernAttachmentRepository extends JpaRepository<ConcernAttachment, Long> {

    long countByConcernId(Long concernId);

    List<ConcernAttachment> findByConcernId(Long concernId);
}

