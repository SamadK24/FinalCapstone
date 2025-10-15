package com.aurionpro.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.ConcernResponse;
import com.aurionpro.dtos.CreateConcernRequest;
import com.aurionpro.entity.Concern;
import com.aurionpro.entity.Concern.ConcernAction;
import com.aurionpro.entity.Concern.ConcernActorRole;
import com.aurionpro.entity.Concern.ConcernStatus;
import com.aurionpro.entity.ConcernAttachment;
import com.aurionpro.entity.ConcernEvent;
import com.aurionpro.exceptions.BusinessRuleException;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.ConcernAttachmentRepository;
import com.aurionpro.repository.ConcernEventRepository;
import com.aurionpro.repository.ConcernRepository;
import com.aurionpro.service.BatchLineService;
import com.aurionpro.service.ConcernService;
import com.aurionpro.service.EmployeeService;
import com.aurionpro.service.NotificationService;
import com.aurionpro.service.PayslipService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcernServiceImpl implements ConcernService {

    private final ConcernRepository concernRepo;
    private final ConcernEventRepository eventRepo;
    private final ConcernAttachmentRepository attachmentRepo;

    private final EmployeeService employeeService;
    private final PayslipService payslipService;
    private final BatchLineService batchLineService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ConcernResponse create(Long orgId, Long employeeId, String actorIdentity, CreateConcernRequest req) {
        employeeService.assertEmployeeInOrg(employeeId, orgId);

        if (req.getPayslipId() != null) {
            payslipService.assertPayslipOwnedBy(req.getPayslipId(), orgId, employeeId);
        }
        if (req.getBatchLineId() != null) {
            batchLineService.assertLineOwnedBy(req.getBatchLineId(), orgId, employeeId);
        }

        Concern concern = Concern.builder()
                .orgId(orgId)
                .employeeId(employeeId)
                .category(req.getCategory())
                .subject(req.getSubject())
                .description(req.getDescription())
                .attachmentRefs(req.getAttachmentRefs() == null ? List.of() : req.getAttachmentRefs())
                .payslipId(req.getPayslipId())
                .batchLineId(req.getBatchLineId())
                .createdBy(actorIdentity)
                .updatedBy(actorIdentity)
                .build();

        concern = concernRepo.save(concern);

        if (req.getAttachmentRefs() != null && !req.getAttachmentRefs().isEmpty()) {
            if (req.getAttachmentRefs().size() > 5) {
                throw new BusinessRuleException("Max 5 attachments allowed");
            }
            for (String ref : req.getAttachmentRefs()) {
                if (ref == null || ref.isBlank() || ref.length() > 300) {
                    throw new BusinessRuleException("Invalid attachment reference");
                }
                attachmentRepo.save(
                        ConcernAttachment.builder()
                                .concernId(concern.getId())
                                .ref(ref)
                                .build()
                );
            }
        }

        ConcernEvent created = eventRepo.save(
                ConcernEvent.builder()
                        .concernId(concern.getId())
                        .actorRole(ConcernActorRole.EMPLOYEE)
                        .action(ConcernAction.CREATED)
                        .note(preview(concern.getSubject(), concern.getDescription()))
                        .build()
        );

        notificationService.notifyOrgAdminsConcernCreated(orgId, concern.getId());

        return toResponse(concern, List.of(created));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConcernResponse> listForEmployee(Long orgId, Long employeeId, List<ConcernStatus> statuses, Pageable pageable) {
        employeeService.assertEmployeeInOrg(employeeId, orgId);
        Page<Concern> page = concernRepo.findByOrgIdAndEmployeeIdAndStatusIn(orgId, employeeId, statusesOrAll(statuses), pageable);
        return page.map(c -> toResponse(c, null));
    }

    @Override
    @Transactional(readOnly = true)
    public ConcernResponse getForEmployee(Long orgId, Long employeeId, Long concernId) {
        employeeService.assertEmployeeInOrg(employeeId, orgId);
        Concern c = concernRepo.findByIdAndOrgIdAndEmployeeId(concernId, orgId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Concern not found"));
        List<ConcernEvent> events = eventRepo.findByConcernIdOrderByCreatedAtDesc(concernId);
        return toResponse(c, events);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConcernResponse> listForOrg(Long orgId, List<ConcernStatus> statuses, Pageable pageable) {
        Page<Concern> page = (statuses == null || statuses.isEmpty())
                ? concernRepo.findByOrgId(orgId, pageable)
                : concernRepo.findByOrgIdAndStatusIn(orgId, statuses, pageable);
        return page.map(c -> toResponse(c, null));
    }

    @Override
    @Transactional(readOnly = true)
    public ConcernResponse getForOrg(Long orgId, Long concernId) {
        Concern c = concernRepo.findByIdAndOrgId(concernId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Concern not found"));
        List<ConcernEvent> events = eventRepo.findByConcernIdOrderByCreatedAtDesc(concernId);
        return toResponse(c, events);
    }

    @Override
    public void addOrgComment(Long orgId, Long concernId, String actor, String note) {
        Concern c = concernRepo.findByIdAndOrgId(concernId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Concern not found"));

        eventRepo.save(ConcernEvent.builder()
                .concernId(c.getId())
                .actorRole(ConcernActorRole.ORGANIZATION_ADMIN)
                .action(ConcernAction.COMMENTED)
                .note(note)
                .build());

        notificationService.notifyEmployeeConcernComment(c.getEmployeeId(), c.getId());
    }

    @Override
    public void updateStatus(Long orgId, Long concernId, ConcernStatus to, String note, String actor) {
        Concern c = concernRepo.findByIdAndOrgId(concernId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Concern not found"));

        ConcernStatus from = c.getStatus();
        validateTransition(from, to, note);

        c.setStatus(to);
        c.setUpdatedBy(actor);
        concernRepo.save(c);

        eventRepo.save(ConcernEvent.builder()
                .concernId(c.getId())
                .actorRole(ConcernActorRole.ORGANIZATION_ADMIN)
                .action(ConcernAction.STATUS_CHANGED)
                .fromStatus(from)
                .toStatus(to)
                .note(note)
                .build());

        if (to == ConcernStatus.RESOLVED || to == ConcernStatus.CLOSED) {
            notificationService.notifyEmployeeConcernResolved(c.getEmployeeId(), c.getId(), to);
        }
    }

    // ------------------ Helper methods ------------------

    private ConcernResponse toResponse(Concern c, List<ConcernEvent> events) {
        List<ConcernResponse.ConcernEventItem> items = events == null ? List.of()
                : events.stream()
                        .map(e -> ConcernResponse.ConcernEventItem.builder()
                                .id(e.getId())
                                .actorRole(e.getActorRole())
                                .action(e.getAction())
                                .note(e.getNote())
                                .fromStatus(e.getFromStatus())
                                .toStatus(e.getToStatus())
                                .createdAt(e.getCreatedAt())
                                .build())
                        .toList();

        return ConcernResponse.builder()
                .id(c.getId())
                .orgId(c.getOrgId())
                .employeeId(c.getEmployeeId())
                .category(c.getCategory())
                .subject(c.getSubject())
                .description(c.getDescription())
                .status(c.getStatus())
                .attachmentRefs(c.getAttachmentRefs())
                .payslipId(c.getPayslipId())
                .batchLineId(c.getBatchLineId())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .events(items)
                .build();
    }

    private List<ConcernStatus> statusesOrAll(List<ConcernStatus> statuses) {
        return (statuses == null || statuses.isEmpty())
                ? List.of(ConcernStatus.OPEN, ConcernStatus.IN_PROGRESS, ConcernStatus.RESOLVED, ConcernStatus.CLOSED)
                : statuses;
    }

    private String preview(String subject, String description) {
        String base = (subject != null && !subject.isBlank()) ? subject : description;
        return base.length() <= 140 ? base : base.substring(0, 140);
    }

    private void validateTransition(ConcernStatus from, ConcernStatus to, String note) {
        switch (from) {
            case OPEN -> require(to == ConcernStatus.IN_PROGRESS, "OPEN can only move to IN_PROGRESS");
            case IN_PROGRESS -> require(to == ConcernStatus.RESOLVED || to == ConcernStatus.CLOSED,
                    "IN_PROGRESS can move to RESOLVED or CLOSED");
            case RESOLVED -> require(to == ConcernStatus.CLOSED, "RESOLVED can only move to CLOSED");
            case CLOSED -> {
                require(to == ConcernStatus.IN_PROGRESS, "CLOSED can only be reopened to IN_PROGRESS");
                require(note != null && note.length() >= 5, "Reopen requires a note");
            }
        }
        if ((to == ConcernStatus.RESOLVED || to == ConcernStatus.CLOSED) &&
                (note == null || note.length() < 5)) {
            throw new BusinessRuleException("Resolution/closure requires a note");
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new BusinessRuleException(message);
    }
}

