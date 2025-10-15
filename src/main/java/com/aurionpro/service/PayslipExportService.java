package com.aurionpro.service;

import com.aurionpro.filters.PayslipFilter;

public interface PayslipExportService {

    /**
     * Export payslips matching the filter to CSV bytes.
     *
     * @param filter  filter criteria
     * @param sortBy  column to sort by
     * @param sortDir "ASC" or "DESC"
     * @return CSV as byte array
     */
    byte[] exportCsv(PayslipFilter filter, String sortBy, String sortDir);
}
