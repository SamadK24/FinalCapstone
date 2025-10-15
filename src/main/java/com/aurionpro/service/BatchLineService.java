package com.aurionpro.service;

public interface BatchLineService {
    void assertLineOwnedBy(Long disbursalLineId, Long orgId, Long employeeId);
}
