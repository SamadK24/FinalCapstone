package com.aurionpro.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.AddCommentRequest;
import com.aurionpro.dtos.ConcernResponse;
//import com.aurionpro.dtos.UpdateAssigneeRequest;
import com.aurionpro.dtos.UpdateConcernStatusRequest;
import com.aurionpro.entity.Concern.ConcernStatus;
import com.aurionpro.service.ConcernService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization/{orgId}/concerns")
public class OrgConcernController {

    private final ConcernService concernService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ConcernResponse>> list(
            @PathVariable Long orgId,
            @RequestParam(required = false) List<ConcernStatus> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return ResponseEntity.ok(concernService.listForOrg(orgId, status, pageable));
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping("/{concernId}")
    public ResponseEntity<ConcernResponse> get(
            @PathVariable Long orgId,
            @PathVariable Long concernId) {

        return ResponseEntity.ok(concernService.getForOrg(orgId, concernId));
    }
    
    @PostMapping("/{concernId}/comments")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public ResponseEntity<Void> addComment(@PathVariable Long orgId,
                                           @PathVariable Long concernId,
                                           @Valid @RequestBody AddCommentRequest req,
                                           Principal principal) {
        concernService.addOrgComment(orgId, concernId, identity(principal), req.getNote());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{concernId}/status")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long orgId,
                                             @PathVariable Long concernId,
                                             @Valid @RequestBody UpdateConcernStatusRequest req,
                                             Principal principal) {
        concernService.updateStatus(orgId, concernId, req.getToStatus(), req.getNote(), identity(principal));
        return ResponseEntity.ok().build();
    }

//    @PatchMapping("/{concernId}/assignee")
//    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
//    public ResponseEntity<Void> assignConcern(@PathVariable Long orgId,
//                                              @PathVariable Long concernId,
//                                              @Valid @RequestBody UpdateAssigneeRequest req,
//                                              Principal principal) {
//        concernService.assign(orgId, concernId, identity(principal), req.getAssigneeUserId(), req.getAssignee());
//        return ResponseEntity.ok().build();
//    }

    private String identity(Principal p) { return p != null ? p.getName() : null; }

}
