package com.aurionpro.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.EmployeeBulkResultDTO;
import com.aurionpro.dtos.EmployeeBulkRowDTO;
import com.aurionpro.dtos.EmployeeCreationDTO;
import com.aurionpro.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// If using OpenCSV:
// import com.opencsv.CSVReader;
// import com.opencsv.bean.CsvToBean;
// import com.opencsv.bean.CsvToBeanBuilder;

@Service
@RequiredArgsConstructor
public class EmployeeBulkService {

    private final EmployeeService employeeService;
    private final UserRepository userRepository;

    public List<EmployeeBulkResultDTO> processCsv(Long orgId, MultipartFile file) throws Exception {
        List<EmployeeBulkRowDTO> rows = parseCsvBasic(file); // or parseCsvWithOpenCsv(file)
        List<EmployeeBulkResultDTO> results = new ArrayList<>();

        Set<String> emailsSeen = new HashSet<>();
        Set<String> codesSeen = new HashSet<>();

        int rowNum = 1; // data rows start at 1 (excluding header)
        for (EmployeeBulkRowDTO row : rows) {
            EmployeeBulkResultDTO res = new EmployeeBulkResultDTO(rowNum++, row.getEmployeeCode(), false, null);
            try {
                requireNonBlank(row.getFullName(), "fullName");
                requireNonBlank(row.getEmail(), "email");
                requireNonBlank(row.getEmployeeCode(), "employeeCode");

                String emailLower = row.getEmail().trim().toLowerCase();
                String code = row.getEmployeeCode().trim();

                if (!emailsSeen.add(emailLower)) {
                    throw new IllegalArgumentException("Duplicate email in file");
                }
                if (!codesSeen.add(code)) {
                    throw new IllegalArgumentException("Duplicate employeeCode in file");
                }
                if (userRepository.existsByEmail(emailLower)) {
                    throw new IllegalArgumentException("Email already exists");
                }
                if (userRepository.existsByUsername(code)) {
                    throw new IllegalArgumentException("EmployeeCode already exists");
                }

                EmployeeCreationDTO dto = new EmployeeCreationDTO();
                dto.setFullName(row.getFullName().trim());
                dto.setEmail(emailLower);
                dto.setEmployeeCode(code);
                // Optional: if dateOfJoining provided, parse and set if your DTO supports it
                if (row.getDateOfJoining() != null && !row.getDateOfJoining().isBlank()) {
                    // parse if EmployeeCreationDTO supports LocalDate; otherwise ignore
                    LocalDate.parse(row.getDateOfJoining().trim()); // validate format
                }

                // Reuse existing single-add flow (handles org approval, roles, email)
                employeeService.addEmployee(orgId, dto);

                res.setSuccess(true);
                res.setMessage("Created");
            } catch (Exception ex) {
                res.setSuccess(false);
                res.setMessage(ex.getMessage());
            }
            results.add(res);
        }
        return results;
    }

    // Basic CSV parser without external libs: expects header line, comma-separated
    private List<EmployeeBulkRowDTO> parseCsvBasic(MultipartFile file) throws Exception {
        List<EmployeeBulkRowDTO> rows = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            BufferedReader br = (BufferedReader) reader;
            String header = br.readLine(); // skip header
            if (header == null) return rows;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                EmployeeBulkRowDTO dto = new EmployeeBulkRowDTO();
                dto.setFullName(get(parts, 0));
                dto.setEmail(get(parts, 1));
                dto.setEmployeeCode(get(parts, 2));
                dto.setDateOfJoining(get(parts, 3));
                rows.add(dto);
            }
        }
        return rows;
    }

    // Example if using OpenCSV instead:
    // private List<EmployeeBulkRowDTO> parseCsvWithOpenCsv(MultipartFile file) throws Exception {
    //     try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
    //         CsvToBean<EmployeeBulkRowDTO> csvToBean = new CsvToBeanBuilder<EmployeeBulkRowDTO>(reader)
    //                 .withType(EmployeeBulkRowDTO.class)
    //                 .withIgnoreLeadingWhiteSpace(true)
    //                 .withSkipLines(1) // skip header if DTO not annotated with bindings
    //                 .build();
    //         return csvToBean.parse();
    //     }
    // }

    private String get(String[] parts, int idx) {
        return idx < parts.length ? parts[idx].trim() : "";
    }

    private void requireNonBlank(String s, String field) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}

