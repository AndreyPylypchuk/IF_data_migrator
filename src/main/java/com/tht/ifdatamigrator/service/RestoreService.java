package com.tht.ifdatamigrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tht.ifdatamigrator.dao.service.RestoreDaoService;
import com.tht.ifdatamigrator.dto.AnswerDTO;
import com.tht.ifdatamigrator.dto.AssessmentDTO;
import com.tht.ifdatamigrator.dto.BackupDTO;
import com.tht.ifdatamigrator.dto.QuestionDTO;
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

        log.info("Restoring finished");
    }

    private void restore(AssessmentDTO a) {
        log.info("Assessment name {}", a.getName());

        Map<String, Object> ids = service.createAssessment(a);
        long assId = parseLong(ids.get("assessment_id").toString());
        long translateId = parseLong(ids.get("translation_id").toString());

        Map<String, String> translate = new HashMap<>();
        translate.put("assessment_desc", null);
        translate.put("assessment_name", a.getName());
        translate.put("candidate_desc", null);
        translate.put("general_instructions", null);
        translate.put("thank_you_desc", null);

        service.createTranslation(translateId, translate);

        a.getQualities().forEach(q -> service.createAssessmentQuality(assId, q));
        a.getQuestions().forEach(q -> service.createAssessmentQuestion(assId, q));
    }

    private void restore(QuestionDTO q) {
        log.info("Question full code {}", q.getCode());

        Map<String, Object> ids = service.createQuestion(q);
        long questionId = parseLong(ids.get("question_id").toString());
        long translateId = parseLong(ids.get("translation_id").toString());

        service.createTranslation(translateId, q.getTranslates(), "question_text");

        q.getAnswers().forEach(a -> restore(a, questionId));
    }

    private void restore(AnswerDTO a, Long questionId) {
        log.info("Question {} answer order {}", questionId, a.getOrder());

        Map<String, Object> ids = service.createAnswer(a, questionId);
        long translateId = parseLong(ids.get("translation_id").toString());

        service.createTranslation(translateId, a.getTranslates(), "answer_text");
    }
}
