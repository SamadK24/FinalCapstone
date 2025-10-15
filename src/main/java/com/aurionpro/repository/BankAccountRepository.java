package com.aurionpro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.BankAccount;
import com.aurionpro.entity.Employee;

import jakarta.persistence.LockModeType;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByOrganizationId(Long organizationId);
    List<BankAccount> findByEmployeeId(Long employeeId);
    
    @Query("select b from BankAccount b where b.organization.id = :orgId and b.verified = true and b.kycStatus = com.aurionpro.entity.BankAccount.KYCDocumentVerificationStatus.VERIFIED")
    Optional<BankAccount> findFirstVerifiedOrgAccount(@Param("orgId") Long orgId); // pick the payroll account policy

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BankAccount b where b.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);
    
    List<BankAccount> findByEmployee(Employee employee);
    
}
