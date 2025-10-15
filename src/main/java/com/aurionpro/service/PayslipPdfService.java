package com.aurionpro.service;

import com.aurionpro.dtos.EmployeePayslipView;

public interface PayslipPdfService {

    /**
     * Render an EmployeePayslipView to PDF bytes.
     *
     * @param view EmployeePayslipView containing all payslip info
     * @return PDF as byte array
     */
    byte[] render(EmployeePayslipView view);
}
