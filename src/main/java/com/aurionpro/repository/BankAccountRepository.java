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
    
 // ✅ Alternative - Spring Data will automatically limit to first result
    Optional<BankAccount> findFirstByOrganizationIdAndKycStatus(Long organizationId, BankAccount.KYCDocumentVerificationStatus kycStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BankAccount b where b.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);
    
    List<BankAccount> findByEmployee(Employee employee);
    
    @Query("SELECT b FROM BankAccount b WHERE b.employee.organization.id = :orgId")
    List<BankAccount> findByEmployeeOrganizationId(@Param("orgId") Long orgId);

    

}
