package com.aurionpro.repository;

import com.aurionpro.entity.ConcernEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConcernEventRepository extends JpaRepository<ConcernEvent, Long> {
	List<ConcernEvent> findByConcernIdOrderByCreatedAtDesc(Long concernId);
}
