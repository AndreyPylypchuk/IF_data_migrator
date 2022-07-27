package com.tht.ifdatamigrator.service.backup;

import com.tht.ifdatamigrator.dao.domain.Company;
import com.tht.ifdatamigrator.dao.service.BackupDaoService;
import com.tht.ifdatamigrator.dto.CompanyDTO;
import com.tht.ifdatamigrator.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@AllArgsConstructor
public class CompanyDataBackupService {

    private final BackupDaoService service;

    public List<CompanyDTO> backupCompanyData() {
        log.info("Company data extracting...");

        Map<String, List<Company>> numCompanies = service.getCompanies()
                .stream()
                .collect(groupingBy(Company::getCustNum));

        List<CompanyDTO> backup = new ArrayList<>();
        for (Map.Entry<String, List<Company>> entry : numCompanies.entrySet()) {
            if (entry.getValue().size() == 1)
                backup.add(toCompanyDto(entry.getValue().get(0)));
            else
                backup.addAll(toCompanyDto(entry.getValue()));
        }

        backup.forEach(companyDto -> {
            companyDto.setUsers(extractCompanyUsers(companyDto));
            companyDto.setAssessmentVersions(extractCompanyAssessments(companyDto));
        });

        backup = backup.stream()
//                .filter(c -> !isEmpty(c.getUsers()))
//                .filter(c -> !isEmpty(c.getAssessmentVersions()))
                .collect(toList());

        log.info("Empty users");
        backup.forEach(c -> {
            if (isEmpty(c.getUsers()))
                System.out.println("CustNum: " + c.getNum() + " Store#: " + c.getStore());
        });

        log.info("Empty ass");
        backup.forEach(c -> {
            if (isEmpty(c.getAssessmentVersions()))
                System.out.println("CustNum: " + c.getNum() + " Store#: " + c.getStore());
        });

        return backup;
    }

    private List<String> extractCompanyAssessments(CompanyDTO companyDto) {
        log.info("Extracting assessments for company {} {}", companyDto.getNum(), companyDto.getStore());
        return service.getCompanyAssessment(companyDto.getNum(), companyDto.getStore())
                .stream()
                .filter(Objects::nonNull)
                .map(v -> v.trim().replaceAll("-", ""))
                .collect(toList());
    }

    private List<UserDTO> extractCompanyUsers(CompanyDTO companyDto) {
        log.info("Extracting users for company {} {}", companyDto.getNum(), companyDto.getStore());
        return service.getUsers(companyDto.getNum(), companyDto.getStore())
                .stream()
                .map(u -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(u.getUserCredentialId());
                    dto.setEmail(trim(u.getEmailAddress()));
                    dto.setPassword(u.getPasswords());
                    dto.setPreviousPassword(u.getPreviousPasswords());
                    dto.setRole(mapRole(u.getRoleID()));
                    return dto;
                })
                .filter(u -> hasText(u.getEmail()))
                .filter(u -> nonNull(u.getRole()))
                .collect(toList());
    }

    private CompanyDTO toCompanyDto(Company company) {
        return new CompanyDTO(
                company.getCustNum(),
                company.getStore(),
                company.getCustName()
        );
    }

    private List<CompanyDTO> toCompanyDto(List<Company> companies) {
        return companies.stream()
                .map(company -> new CompanyDTO(
                        company.getCustNum(),
                        company.getStore(),
                        company.getCustName() + " - " + company.getStore())
                ).collect(toList());
    }

    private String mapRole(Integer roleID) {
        if (roleID == 2) return "myaccount_admin";
        if (roleID == 3) return "myaccount_editor";
        return null;
    }

    private String trim(String str) {
        if (isNull(str)) return null;
        return str.trim();
    }
}
