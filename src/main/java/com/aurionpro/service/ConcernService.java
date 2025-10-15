package com.aurionpro.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.aurionpro.dtos.ConcernResponse;
import com.aurionpro.dtos.CreateConcernRequest;
import com.aurionpro.entity.Concern.ConcernStatus;

public interface ConcernService {
    
    ConcernResponse create(Long orgId, Long employeeId, String actorIdentity, CreateConcernRequest req);

    Page<ConcernResponse> listForEmployee(Long orgId, Long employeeId, List<ConcernStatus> statuses, Pageable pageable);

    ConcernResponse getForEmployee(Long orgId, Long employeeId, Long concernId);

    Page<ConcernResponse> listForOrg(Long orgId, List<ConcernStatus> statuses, Pageable pageable);

    ConcernResponse getForOrg(Long orgId, Long concernId);

    void addOrgComment(Long orgId, Long concernId, String actor, String note);

    void updateStatus(Long orgId, Long concernId, ConcernStatus to, String note, String actor);
    
    // Optionally add more methods like assign(), etc.
}
