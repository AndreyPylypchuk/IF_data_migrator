package com.tht.ifdatamigrator.service.backup;

import com.tht.ifdatamigrator.dao.domain.Company;
import com.tht.ifdatamigrator.dao.service.BackupDaoService;
import com.tht.ifdatamigrator.dto.ApplicantDTO;
import com.tht.ifdatamigrator.dto.CompanyDTO;
import com.tht.ifdatamigrator.dto.CompanyDTO.AssessmentData;
import com.tht.ifdatamigrator.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tht.ifdatamigrator.Const.IMPACTS;
import static com.tht.ifdatamigrator.Const.MIGRATED_COMPANIES;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyDataBackupService {

    private static final Pattern BUSINESS_IMPACT_PATTERN = compile("([A-Z].+)");
    private static final Pattern NAME_PART_PATTERN = compile("([a-zA-z]{2,})");
    private static final Pattern DIGIT_PATTERN = compile("\\d");

    @Value("${migration.scopes}")
    private List<String> scopes;

    private final BackupDaoService service;

    public List<CompanyDTO> backupCompanyData() {
        log.info("Company data extracting...");

        List<CompanyDTO> companies = service.getCompanies()
                .stream()
                .map(this::toCompanyDto)
                .collect(toList());

        if (scopes.contains("companyData"))
            companies = companies.stream()
                    .peek(c -> c.setUsers(extractCompanyUsers(c)))
                    .filter(c -> !isEmpty(c.getUsers()))
                    .collect(toList());

        companies = companies.stream()
                .peek(c -> c.setAssessmentData(extractCompanyAssessments(c)))
                .collect(toList());

        Map<String, List<CompanyDTO>> numCompanies = companies
                .stream()
                .collect(groupingBy(CompanyDTO::getNum));

        for (Map.Entry<String, List<CompanyDTO>> entry : numCompanies.entrySet()) {
            if (entry.getValue().size() > 1) {
                entry.getValue()
                        .forEach(c -> c.setName("%s - %s (%s)".formatted(
                                c.getName(), trim(c.getStoreName()), c.getStore()
                        )));
            }
        }

        return companies;
    }

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

        if (scopes.contains("companyApplicant")) {
            assessmentData.forEach(v -> {
                var applicants = service.getApplicants(companyDto.getNum(), companyDto.getStore(), v.getAtiVersion());
                v.setApplicants(map(applicants));
            });
        }

        return assessmentData;
    }

    private List<ApplicantDTO> map(List<Map<String, Object>> applicants) {
        return applicants.stream()
                .map(this::map)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private ApplicantDTO map(Map<String, Object> applicant) {
        String phone = get(applicant.get("Phone"));
        String ssn = get(applicant.get("SSN"));
        String firstName = get(applicant.get("FirstName"));
        String lastName = get(applicant.get("LastName"));

        if (isNull(phone) && isNull(ssn) && isNull(firstName) && isNull(lastName))
            return null;

        ApplicantDTO dto = new ApplicantDTO();

        if (nonNull(ssn) && ssn.contains("@"))
            dto.setEmail(ssn);

        if (nonNull(phone))
            dto.setPhone("1" + phone);

        if (nonNull(firstName) && nonNull(lastName)) {
            dto.setFirstName(firstName);
            dto.setLastName(lastName);
        }

        if (isNull(dto.getFirstName()) && isNull(dto.getLastName())) {
            if (nonNull(dto.getEmail()))
                dto.setLastName(dto.getEmail());
            else if (nonNull(ssn)) {
                if (!DIGIT_PATTERN.matcher(ssn).find()) {
                    List<String> nameParts = new LinkedList<>();
                    Matcher nameMatcher = NAME_PART_PATTERN.matcher(ssn);
                    while (nameMatcher.find()) {
                        nameParts.add(nameMatcher.group());
                    }
                    if (nameParts.size() == 1)
                        dto.setLastName(nameParts.get(0));
                    else if (nameParts.size() > 1) {
                        dto.setFirstName(nameParts.get(0));
                        dto.setLastName(nameParts.get(1));
                    }
                }
            }
        }

        if (isNull(dto.getFirstName()) && isNull(dto.getLastName()) && nonNull(dto.getPhone()))
            dto.setLastName("Phone Number - " + dto.getPhone());

        if (isNull(dto.getPhone()) && isNull(dto.getEmail()) && isNull(dto.getFirstName()) && isNull(dto.getLastName()))
            return null;

        dto.setId(parseLong(applicant.get("RowID").toString()));

        Timestamp testDate = (Timestamp) applicant.get("TestDate");
        dto.setTestDate(testDate.toLocalDateTime());

        Timestamp testStart = (Timestamp) applicant.get("TestStart");
        dto.setTestStart(testStart.toLocalDateTime());

        dto.setResult(applicant.get("Overall").toString());
        dto.setDrugs(applicant.get("Drugs").toString());
        dto.setFaking(applicant.get("Faking").toString());
        dto.setTheft(applicant.get("Theft").toString());
        dto.setHostility(applicant.get("Hostility").toString());

        List<String> facts = extractBusinessImpacts(applicant.get("add_data").toString());
        if (!isEmpty(facts)) {
            List<String> businessImpacts = new ArrayList<>();
            List<String> disclosures = new ArrayList<>();
            facts.forEach(f -> {
                if (IMPACTS.contains(f)) businessImpacts.add(f);
                else disclosures.add(f);
            });
            dto.setBusinessImpacts(toImpact(businessImpacts));
            dto.setDisclosures(toImpact(disclosures));
        }

        Map<Integer, Integer> questionAnswers = new HashMap<>();
        for (int i = 1; i <= 110; i++) {
            String key = "Q" + i;
            Object answer = applicant.get(key);

            if (isNull(answer))
                questionAnswers.put(i, null);
            else
                try {
                    questionAnswers.put(i, parseInt(answer.toString().trim()));
                } catch (Exception e) {
                    questionAnswers.put(i, null);
                }
        }
        dto.setQuestionAnswer(questionAnswers);

        return dto;
    }

    private String toImpact(List<String> facts) {
        if (isEmpty(facts))
            return "<ul></ul>";

        return "<ul>" +
                facts.stream()
                        .map(p -> "<li>" + p + "</li>")
                        .collect(joining("")) +
                "</ul>";
    }

    private List<String> extractBusinessImpacts(String addInfo) {
        String cleanAddInfo = addInfo.replaceAll("[\u0000-\u001F\u007F-\uFFFF]", " ");

        if (!cleanAddInfo.contains("BUSINESSIMPACT"))
            return emptyList();

        String data = substringAfter(cleanAddInfo, "#MagnetCore.Entities.ModelCategory[]");

        if (!hasText(data))
            return emptyList();

        data = substringBefore(data, "&MagnetCore.Entities.MagnetQuestionFull");

        if (!hasText(data))
            return emptyList();

        List<String> parts = of(data.split(".BUSINESSIMPACT."))
                .filter(StringUtils::hasText)
                .filter(p -> p.contains("."))
                .map(String::trim)
                .flatMap(p -> of(p.split("\\.")))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(p -> p.length() > 5)
                .collect(toList());

        return parts.stream()
                .flatMap(p -> {
                    ArrayList<String> impacts = new ArrayList<>();
                    Matcher matcher = BUSINESS_IMPACT_PATTERN.matcher(p);
                    while (matcher.find()) {
                        impacts.add(matcher.group());
                    }
                    return impacts.stream();
                })
                .filter(Objects::nonNull)
                .map(p -> p + ".")
                .collect(toList());
    }

    private String get(Object o) {
        if (isNull(o)) return null;
        String tr = o.toString().trim();
        if (!hasText(tr)) return null;
        return tr;
    }

    private Set<UserDTO> extractCompanyUsers(CompanyDTO companyDto) {
        log.info("Extracting users for company {} {}", companyDto.getNum(), companyDto.getStore());
        Set<UserDTO> users = service.getUsers(companyDto.getNum(), companyDto.getStore())
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
                .collect(toSet());

        users.addAll(
                MIGRATED_COMPANIES.get(companyDto.getNum()).getUsers().entrySet()
                        .stream()
                        .map(e -> {
                            UserDTO dto = new UserDTO();
                            dto.setId(null);
                            dto.setEmail(e.getKey());
                            dto.setRole(e.getValue());
                            return dto;
                        })
                        .collect(toSet())
        );

        return users;
    }

    private CompanyDTO toCompanyDto(Company company) {
        return new CompanyDTO(
                company.getCustNum(),
                company.getStore(),
                company.getCustName(),
                company.getStoreName(),
                MIGRATED_COMPANIES.get(company.getCustNum()).getThtId()
        );
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
