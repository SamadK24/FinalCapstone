package com.aurionpro.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.ConcernResponse;
import com.aurionpro.dtos.CreateConcernRequest;
import com.aurionpro.entity.Concern.ConcernStatus;
import com.aurionpro.service.ConcernService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization/{orgId}/employees/{employeeId}/concerns")
public class EmployeeConcernController {

    private final ConcernService concernService;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<ConcernResponse> create(
            @PathVariable Long orgId,
            @PathVariable Long employeeId,
            @Valid @RequestBody CreateConcernRequest req,
            Principal principal) {

        String actorIdentity = extractActorIdentity(principal);
        return ResponseEntity.ok(concernService.create(orgId, employeeId, actorIdentity, req));
    }

    // helper method
    private String extractActorIdentity(Principal principal) {
        return principal != null ? principal.getName() : null;
    }


    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    public ResponseEntity<Page<ConcernResponse>> list(
            @PathVariable Long orgId,
            @PathVariable Long employeeId,
            @RequestParam(required = false) List<ConcernStatus> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return ResponseEntity.ok(concernService.listForEmployee(orgId, employeeId, status, pageable));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{concernId}")
    public ResponseEntity<ConcernResponse> get(
            @PathVariable Long orgId,
            @PathVariable Long employeeId,
            @PathVariable Long concernId) {

        return ResponseEntity.ok(concernService.getForEmployee(orgId, employeeId, concernId));
    }

    private Long extractUserId(Principal principal) {
        // Adapt to your JWT principal model
        return Long.parseLong(principal.getName());
    }
}
