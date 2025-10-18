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

    List<BankAccount> findByEmployee(Employee employee);

    @Query("SELECT b FROM BankAccount b WHERE b.employee.organization.id = :orgId")
    List<BankAccount> findByEmployeeOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT b FROM BankAccount b WHERE b.organization.id = :orgId AND b.verified = true AND b.kycStatus = com.aurionpro.entity.BankAccount.KYCDocumentVerificationStatus.VERIFIED")
    Optional<BankAccount> findFirstVerifiedOrgAccount(@Param("orgId") Long orgId);

    Optional<BankAccount> findFirstByOrganizationIdAndKycStatus(Long organizationId, BankAccount.KYCDocumentVerificationStatus kycStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankAccount b WHERE b.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT COUNT(b) > 0 FROM BankAccount b WHERE b.accountNumber = :accountNumber AND b.ifscCode = :ifscCode AND (:employeeId IS NULL OR b.employee.id != :employeeId) AND (:orgId IS NULL OR b.organization.id != :orgId)")
    boolean existsDuplicateAccount(@Param("accountNumber") String accountNumber,
                                   @Param("ifscCode") String ifscCode,
                                   @Param("employeeId") Long employeeId,
                                   @Param("orgId") Long orgId);

    @Query("SELECT b FROM BankAccount b WHERE b.employee.id = :employeeId AND b.isPrimary = true AND b.verified = true")
    Optional<BankAccount> findPrimaryAccountByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT b FROM BankAccount b WHERE b.employee = :employee ORDER BY b.createdAt DESC")
    List<BankAccount> findByEmployeeOrderByCreatedAtDesc(@Param("employee") Employee employee);
}
