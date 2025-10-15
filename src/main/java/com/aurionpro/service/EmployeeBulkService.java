package com.aurionpro.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.EmployeeBulkResultDTO;

public interface EmployeeBulkService {

    /**
     * Process a CSV file of employee records for bulk creation.
     * @param orgId Organization ID
     * @param file CSV file
     * @return List of results per row
     */
    List<EmployeeBulkResultDTO> processCsv(Long orgId, MultipartFile file) throws Exception;
}
