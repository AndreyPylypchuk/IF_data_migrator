package com.tht.ifdatamigrator.service.backup;

import com.tht.ifdatamigrator.dao.domain.Company;
import com.tht.ifdatamigrator.dao.service.BackupDaoService;
import com.tht.ifdatamigrator.dto.CompanyDTO;
import com.tht.ifdatamigrator.dto.CompanyDTO.AssessmentData;
import com.tht.ifdatamigrator.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.tht.ifdatamigrator.Const.MIGRATED_COMPANIES;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyDataBackupService {

    @Value("${migration.scopes}")
    private List<String> scopes;

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

        backup = backup.stream()
                .peek(c -> c.setUsers(extractCompanyUsers(c)))
                .filter(c -> !isEmpty(c.getUsers()))
                .collect(toList());

        backup = backup.stream()
                .peek(c -> c.setAssessmentData(extractCompanyAssessments(c)))
                .filter(c -> !isEmpty(c.getAssessmentData()))
                .collect(toList());

        return backup;
    }

//    private List<ApplicantDTO> map(List<Map<String, Object>> applicants) {
//        return applicants.stream().map(this::map).collect(toList());
//    }

//    private ApplicantDTO map(Map<String, Object> applicant) {
//        ApplicantDTO dto = new ApplicantDTO();
//
//        RawBson
//
//        JSONObject o = new JSONObject(new String((byte[]) applicant.get("FullSurvey")));
//
//        Timestamp date = (Timestamp) applicant.get("TestDate");
//        dto.setAssessmentDate(date.toLocalDateTime());
//
//        Map<Integer, Integer> questionAnswers = new HashMap<>();
//        for (int i = 1; i <= 110; i++) {
//            String key = "Q" + i;
//            Object answer = applicant.get(key);
//
//            if (isNull(answer))
//                questionAnswers.put(i, null);
//            else
//                try {
//                    questionAnswers.put(i, parseInt(answer.toString().trim()));
//                } catch (Exception e) {
//                    questionAnswers.put(i, null);
//                }
//        }
//        dto.setQuestionAnswer(questionAnswers);
//
//        return dto;
//    }

    private List<AssessmentData> extractCompanyAssessments(CompanyDTO companyDto) {
        log.info("Extracting assessments for company {} {}", companyDto.getNum(), companyDto.getStore());

        List<AssessmentData> assessmentData = service.getCompanyAssessment(companyDto.getNum(), companyDto.getStore())
                .stream()
                .filter(Objects::nonNull)
                .map(v -> {
                    AssessmentData av = new AssessmentData();
                    av.setAtiVersion(v.trim());
                    av.setThtVersion(v.trim().replaceAll("-", ""));
                    return av;
                })
                .collect(toList());

        assessmentData.forEach(v -> {
            var applicants = service.getApplicants(companyDto.getNum(), companyDto.getStore(), v.getAtiVersion());
            v.setHasApplicants(!isEmpty(applicants));

            if (scopes.contains("companyApplicant")) {
                //TODO:parse applicants
            }
        });

        return assessmentData;
    }

    private List<UserDTO> extractCompanyUsers(CompanyDTO companyDto) {
        log.info("Extracting users for company {} {}", companyDto.getNum(), companyDto.getStore());
        return service.getUsers(companyDto.getNum(), companyDto.getStore())
                .stream()
                .map(u -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(u.getUserCredentialId());
                    dto.setEmail(trim(u.getEmailAddress()));
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
                company.getCustName(),
                MIGRATED_COMPANIES.get(company.getCustNum())
        );
    }

    private List<CompanyDTO> toCompanyDto(List<Company> companies) {
        return companies.stream()
                .map(company -> new CompanyDTO(
                        company.getCustNum(),
                        company.getStore(),
                        company.getCustName() + " - " + company.getStore(),
                        MIGRATED_COMPANIES.get(company.getCustNum()))
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
