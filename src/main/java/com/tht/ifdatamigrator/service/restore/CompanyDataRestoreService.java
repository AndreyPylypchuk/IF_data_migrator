package com.tht.ifdatamigrator.service.restore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tht.ifdatamigrator.dao.service.RestoreDaoService;
import com.tht.ifdatamigrator.dto.CompanyDTO;
import com.tht.ifdatamigrator.dto.CompanyDTO.AssessmentData;
import com.tht.ifdatamigrator.dto.CreateJobpostResponse;
import com.tht.ifdatamigrator.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyDataRestoreService {

    @Value("${migration.jobpost.url}")
    private String jobPostCreateUrl;

    private final RestoreDaoService service;
    private final RestTemplate template;

    public void restore(CompanyDTO companyDTO) {
        log.info("Company name {}", companyDTO.getName());

        Long adminId;
        Long companyId;
        if (nonNull(companyDTO.getThtId())) {
            companyId = companyDTO.getThtId();
            adminId = service.getAdminId(companyDTO.getThtId());
        } else {
            UserDTO admin = companyDTO.getUsers()
                    .stream()
                    .filter(u -> "myaccount_admin".equals(u.getRole()))
                    .findFirst()
                    .orElseGet(() -> {
                        UserDTO a = new UserDTO();
                        a.setEmail("leisa@ambroseair.com");
                        a.setRole("myaccount_admin");
                        return a;
                    });

            String adminEmail = admin.getEmail();
            adminId = service.getUserIdByEmail(admin.getEmail());

            if (isNull(adminId))
                adminId = service.createUser(admin);

            Map<String, Object> param = new HashMap<>();
            param.put("ati_cust", companyDTO.getNum());
            param.put("ati_store", companyDTO.getStore());
            companyId = service.getId("company", "company_id", param);
            if (isNull(companyId)) {
                Map<String, Object> ids = service.createCompany(companyDTO, adminId, adminEmail);
                companyId = parseLong(ids.get("company_id").toString());
            }
        }

        handleUsers(companyId, companyDTO.getUsers());
        handleAss(companyId, adminId, companyDTO.getAssessmentData());
    }

    @SneakyThrows
    private CreateJobpostResponse createJobPost(String jobpostTitle, Long companyId, Long adminId) {
        Map<String, Object> params = new HashMap<>();
        params.put("manager_id", adminId);
        params.put("company_id", companyId);
        params.put("jobpost_title", jobpostTitle);
        params.put("jobpost_description", jobpostTitle);
        return template.postForObject(jobPostCreateUrl, new ObjectMapper().writeValueAsString(params), CreateJobpostResponse.class);
    }

    private void handleUsers(Long companyId, List<UserDTO> users) {
        for (UserDTO u : users) {
            log.info("Handling user {}", u.getEmail());
            Long userId = service.getUserIdByEmail(u.getEmail());
            if (isNull(userId))
                userId = service.createUser(u);

            Map<String, Object> myAccParam = new HashMap<>();
            myAccParam.put("user_id", userId);
            myAccParam.put("company_id", companyId);
            Long myAccUserId = service.getId("user_myaccount", "user_myaccount_id", myAccParam);
            if (isNull(myAccUserId))
                service.createMyAccountUser(userId, companyId, u.getRole());
        }
    }

    private void handleAss(Long companyId, Long adminId, List<AssessmentData> data) {
        for (AssessmentData ass : data) {
            log.info("Handling assessment {}", ass.getThtVersion());
            service.createCompanyAssessment(companyId, ass.getThtVersion());

            if (!ass.isHasApplicants())
                return;

            String jobpostTitle = format("IntegrityFirst (%s)", ass.getThtVersion());

            Map<String, Object> companyJobpostParam = new HashMap<>();
            companyJobpostParam.put("jobpost_title", jobpostTitle);
            companyJobpostParam.put("company_id", companyId);
            Long comJobpostId = service.getId(
                    "company_jobposting",
                    "company_jobposting_id",
                    companyJobpostParam
            );
            Long companyJobpostingStatusId;
            if (isNull(comJobpostId)) {
                CreateJobpostResponse response = createJobPost(jobpostTitle, companyId, adminId);
                comJobpostId = response.getCompanyJobpostingId();
                companyJobpostingStatusId = response.getStatuses()
                        .stream()
                        .filter(s -> "assessment".equals(s.getStatusType()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Not found 'assessment' status after creating jobpost"))
                        .getCompanyJobpostingStatusId();
            } else {
                companyJobpostingStatusId = service.getCompanyJobpostingStatusId(comJobpostId);
            }

            Long assId = service.getAssessment(ass.getThtVersion());
            if (assId == null)
                throw new RuntimeException("Not found ass from ATI with version " + ass.getThtVersion());

            Map<String, Object> comJobpostStatusAssIdParam = new HashMap<>();
            comJobpostStatusAssIdParam.put("company_jobposting_status_id", companyJobpostingStatusId);
            comJobpostStatusAssIdParam.put("assessment_id", assId);
            Long compJobPostStatusAssId = service.getId(
                    "company_jobposting_status_assessment",
                    "company_jobposting_status_assessment_id",
                    comJobpostStatusAssIdParam
            );
            if (compJobPostStatusAssId == null)
                service.createCompanyJobpostStatusAss(companyJobpostingStatusId, assId);
        }
    }
}
