package com.aurionpro.entity;

import java.time.LocalDate;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employees",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "employeeCode")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {

    public enum Status { ACTIVE, INACTIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String employeeCode;

    private LocalDate dateOfJoining;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_template_id")
    private SalaryTemplate salaryTemplate;

    @Column
    private Double overrideBasicSalary;
    @Column
    private Double overrideHra;
    @Column
    private Double overrideAllowances;
    @Column
    private Double overrideDeductions;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BankAccount> bankAccounts;

    @Column
    private String designation;
    @Column
    private String department;

    // Added for profile
    @Column(length = 15)
    private String phone;

    @Column(length = 100)
    private String altEmail;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;
}
