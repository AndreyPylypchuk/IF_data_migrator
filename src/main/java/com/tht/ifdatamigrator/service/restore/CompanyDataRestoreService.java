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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.List.of;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyDataRestoreService {

    @Value("${migration.jobpost.url}")
    private String jobPostCreateUrl;

    @Value("${migration.todolist.url}")
    private String todoListCreateUrl;

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
                        companyDTO.getUsers().add(a);
                        return a;
                    });

            adminId = service.getUserIdByEmail(admin.getEmail());

            if (isNull(adminId))
                adminId = service.createUser(admin);

            companyId = service.createCompany(companyDTO, adminId, admin.getEmail());

            service.createCompanyStatus(companyId);
            service.createCompanyEmailTemplate(companyId);
            service.createCompanyReference(companyId);
        }

        handleUsers(companyId, companyDTO.getUsers());
        handleAss(companyId, adminId, companyDTO.getAssessmentData());
    }

    @SneakyThrows
    private CreateJobpostResponse createJobPost(String jobpostTitle, Long companyId, Long adminId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("manager_id", adminId);
        params.put("company_id", companyId);
        params.put("jobpost_title", jobpostTitle);
        params.put("jobpost_description", jobpostTitle);

        HttpEntity<String> entity = new HttpEntity<>(
                new ObjectMapper().writeValueAsString(params),
                headers
        );

        return template.postForObject(jobPostCreateUrl, entity, CreateJobpostResponse.class);
    }

    private void handleUsers(Long companyId, Set<UserDTO> users) {
        for (UserDTO u : users) {
            log.info("Handling user {}", u.getEmail());
            Long userId = service.getUserIdByEmail(u.getEmail());
            if (isNull(userId))
                userId = service.createUser(u);

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
                comJobpostId = response.getData().getCompanyJobpostingId();

                companyJobpostingStatusId = response.getData().getStatuses()
                        .stream()
                        .filter(s -> "assessment".equals(s.getStatusType()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Not found 'assessment' status after creating jobpost"))
                        .getCompanyJobpostingStatusId();
            } else {
                companyJobpostingStatusId = service.getCompanyJobpostingStatusId(comJobpostId);
            }

            service.createJobPostNotificationSettings(comJobpostId);

            Long assId = service.getAssessment(ass.getThtVersion());
            if (assId == null)
                throw new RuntimeException("Not found ass from ATI with version " + ass.getThtVersion());

            service.createCompanyJobpostStatusAss(companyJobpostingStatusId, assId);
            createTodoList(assId, comJobpostId, companyId, adminId);
        }
    }

    @SneakyThrows
    private void createTodoList(Long assId, Long comJobpostId, Long companyId, Long adminId) {
        Long todoListId = service.getId(
                "company_jobposting_todo_list",
                "company_jobposting_todo_list_id",
                singletonMap("company_jobposting_id", comJobpostId)
        );
        if (nonNull(todoListId))
            return;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("manager_id", adminId);
        params.put("company_id", companyId);
        params.put("company_jobposting_id", comJobpostId);
        params.put("assessment_ids", of(assId));

        HttpEntity<String> entity = new HttpEntity<>(
                new ObjectMapper().writeValueAsString(params),
                headers
        );

        template.postForEntity(todoListCreateUrl, entity, String.class);
    }
}
