package com.tht.ifdatamigrator.service.restore;

import com.tht.ifdatamigrator.dao.service.RestoreDaoService;
import com.tht.ifdatamigrator.dto.ApplicantDTO;
import com.tht.ifdatamigrator.dto.CompanyDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.tht.ifdatamigrator.service.restore.RestoreUtils.generateJobpostTitle;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@AllArgsConstructor
public class ApplicantRestoreService {

    private final RestoreDaoService service;

    @Transactional
    public void restore(CompanyDTO companyDTO) {
        log.info("Restoring company {} {} applicants", companyDTO.getNum(), companyDTO.getStore());

        Long companyId = getCompanyId(companyDTO);

        companyDTO.getAssessmentData().forEach(ass -> {
            log.info("Restoring assessment {}", ass.getThtVersion());

            Long assId = getAssessmentId(ass.getThtVersion());
            String jobpostTitle = generateJobpostTitle(ass.getThtVersion());
            Long cjId = getCompanyJobpostId(companyId, jobpostTitle);
            Long cjManagerId = service.getCompanyJobpostingManagerId(cjId);
            Long cjsId = getCompanyJobpostStatusId(cjId);

            var qOrderId = service.getAssessmentQuestions(assId);

            ass.getApplicants().forEach(app -> {
                log.info("Restoring applicant {}", app.getEmail());

                Long userId = getUserId(app);

                Long cjcId = service.createCompanyJobpostingCandidate(app, userId, cjId, cjsId);
                service.createCompanyJobpostingCandidateEmail(cjcId, app.getEmail());
                service.createCompanyJobpostingCandidatePhone(cjcId, app.getPhone());

                Long cjcaId = service.createCompanyJobpostingCandidateAss(
                        cjcId, assId, ass.getThtVersion(), app, cjManagerId
                );

                app.getQuestionAnswer()
                        .forEach((key, value) -> {
                            Long qId = qOrderId.get(key);
                            if (nonNull(qId) && nonNull(value))
                                createQuestionAnswer(cjcaId, userId, qId, value, app.getTestDate());
                        });
            });
        });
    }

    private void createQuestionAnswer(Long cjcaId, Long userId, Long qId, Integer answer, LocalDateTime date) {
        log.info("Restoring question {} {} {}", cjcaId, qId, answer);

        Long aId = service.getAnswerId(qId, answer);
        if (isNull(aId))
            throw new RuntimeException("Not found answer [questionId=" + qId + ",answerOrder=" + answer + "]");

        service.creteQuestionAnswer(cjcaId, qId, aId, date, userId);
    }

    private Long getAssessmentId(String version) {
        Long id = service.getAssessment(version);
        if (id == null)
            throw new RuntimeException("Assessment not found " + version);
        return id;
    }

    private Long getCompanyId(CompanyDTO companyDTO) {
        Map<String, Object> companyParam = new HashMap<>();
        companyParam.put("ati_cust", companyDTO.getNum());
        companyParam.put("ati_store", companyDTO.getStore());
        Long companyId = service.getId("company", "company_id", companyParam);
        if (companyId == null)
            throw new RuntimeException("Company not migrated");
        return companyId;
    }

    private Long getCompanyJobpostId(Long companyId, String jobpostTitle) {
        Map<String, Object> companyJobpostParam = new HashMap<>();
        companyJobpostParam.put("jobpost_title", jobpostTitle);
        companyJobpostParam.put("company_id", companyId);

        Long cjId = service.getId(
                "company_jobposting",
                "company_jobposting_id",
                companyJobpostParam
        );

        if (cjId == null)
            throw new RuntimeException("Company jobposting not found " + companyJobpostParam);

        return cjId;
    }

    private Long getCompanyJobpostStatusId(Long cjId) {
        Long cjsId = service.getCompanyJobpostingStatusIdByName(cjId, "New candidate");
        if (cjsId == null)
            throw new RuntimeException("Company jobposting status not found. Company jobposting id " + cjId);
        return cjsId;
    }

    private Long getUserId(ApplicantDTO app) {
        Long userId = -1L;
        if (nonNull(app.getEmail())) {
            userId = service.getUserIdByEmail(app.getEmail());
            if (userId == null)
                userId = service.createUser(app);
        }
        return userId;
    }
}
