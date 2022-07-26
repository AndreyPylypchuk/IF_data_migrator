package com.tht.ifdatamigrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tht.ifdatamigrator.dao.service.RestoreDaoService;
import com.tht.ifdatamigrator.dto.*;
import com.tht.ifdatamigrator.dto.AssessmentDTO.AssQuestionDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreService {

    @Value("${migration.scopes}")
    private List<String> scopes;

    private final RestoreDaoService service;

    @SneakyThrows
    public void restore() {
        log.info("Restoring started");

        BackupDTO data = new ObjectMapper().readValue(new File("backup.json"), BackupDTO.class);

        if (scopes.contains("questionAnswerData")) data.getQuestionAnswerData().forEach(this::restore);
        if (scopes.contains("assessmentData")) data.getAssessmentData().forEach(this::restore);
        if (scopes.contains("companyData")) data.getCompanyData().forEach(this::restore);

        log.info("Restoring finished");
    }

    private void restore(CompanyDTO companyDTO) {
        log.info("Company name {}", companyDTO.getName());

        UserDTO admin = companyDTO.getUsers()
                .stream()
                .filter(u -> "myaccount_admin".equals(u.getRole()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Not found admin user for [num:" + companyDTO.getNum() + " store" + companyDTO.getStore()
                ));

        String adminEmail = admin.getEmail();
        Long adminId = service.getUserIdByEmail(admin.getEmail());
        if (isNull(adminId))
            adminId = service.createUser(admin);

        Map<String, Object> param = new HashMap<>();
        param.put("ati_cust", companyDTO.getNum());
        param.put("ati_store", companyDTO.getStore());
        Long companyId = service.getId("company", "company_id", param);
        if (isNull(companyId)) {
            Map<String, Object> ids = service.createCompany(companyDTO, adminId, adminEmail);
            companyId = parseLong(ids.get("company_id").toString());
        }

        for (UserDTO u : companyDTO.getUsers()) {
            log.info("Handling user {}", u.getEmail());
            Long userId = service.getUserIdByEmail(u.getEmail());
            if (isNull(userId))
                //TODO: handle password
                userId = service.createUser(u);

            Map<String, Object> myAccParam = new HashMap<>();
            param.put("user_id", userId);
            param.put("company_id", companyId);
            Long myAccUserId = service.getId("user_myaccount", "user_myaccount_id", myAccParam);
            if (isNull(myAccUserId))
                service.createMyAccountUser(userId, companyId, u.getRole());
        }

        for (String assVersion : companyDTO.getAssessmentVersions()) {
            log.info("Handling assessment {}", assVersion);
            service.createCompanyAssessment(companyId, assVersion);

            //TODO: handle job posts
        }
    }

    private void restore(AssessmentDTO a) {
        log.info("Assessment name {}", a.getName());

        Map<String, Object> param = new HashMap<>();
        param.put("assessment_name", a.getName());
        param.put("from_ati", true);
        Long assId = service.getId("assessment", "assessment_id", param);
        if (isNull(assId)) {
            Map<String, Object> ids = service.createAssessment(a);
            assId = parseLong(ids.get("assessment_id").toString());
            long translateId = parseLong(ids.get("translation_id").toString());

            Map<String, String> translate = new HashMap<>();
            translate.put("assessment_desc", null);
            translate.put("assessment_name", a.getName());
            translate.put("candidate_desc", null);
            translate.put("general_instructions", null);
            translate.put("thank_you_desc", null);

            service.createTranslation(translateId, translate);
        }

        for (Integer qId : a.getQualities()) {
            Map<String, Object> qParam = new HashMap<>();
            param.put("assessment_id", assId);
            param.put("quality_id", qId);
            Long id = service.getId("assessment_quality", "assessment_quality_id", qParam);
            if (isNull(id))
                service.createAssessmentQuality(assId, qId);
        }
        for (AssQuestionDTO q : a.getQuestions()) {
            Map<String, Object> qParam = new HashMap<>();
            param.put("ati_full_code", q.getAtiFullCode());
            Long qId = service.getId("question", "question_id", qParam);
            if (isNull(qId)) continue;

            Map<String, Object> aqParam = new HashMap<>();
            param.put("assessment_id", assId);
            param.put("question_id", qId);
            Long id = service.getId("assessment_question", "assessment_question_id", aqParam);
            if (isNull(id))
                service.createAssessmentQuestion(assId, qId, q);
        }
    }

    private void restore(QuestionDTO q) {
        log.info("Question full code {}", q.getCode());

        Long questionId = service.getId("question", "question_id", singletonMap("ati_code", q.getId()));
        if (isNull(questionId)) {
            Map<String, Object> ids = service.createQuestion(q);
            questionId = parseLong(ids.get("question_id").toString());
            long translateId = parseLong(ids.get("translation_id").toString());

            service.createTranslation(translateId, q.getTranslates(), "question_text");
        }

        for (AnswerDTO a : q.getAnswers()) {
            restore(a, questionId);
        }
    }

    private void restore(AnswerDTO a, Long questionId) {
        log.info("Question {} answer order {}", questionId, a.getOrder());

        Map<String, Object> param = new HashMap<>();
        param.put("question_id", questionId);
        param.put("answer_text", a.getText());
        Long answerId = service.getId("question_answer", "question_answer_id", param);

        if (isNull(answerId)) {
            Map<String, Object> ids = service.createAnswer(a, questionId);
            long translateId = parseLong(ids.get("translation_id").toString());

            service.createTranslation(translateId, a.getTranslates(), "answer_text");
        }
    }
}
