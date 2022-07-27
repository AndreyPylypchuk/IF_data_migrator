package com.tht.ifdatamigrator.service.restore;

import com.tht.ifdatamigrator.dao.service.RestoreDaoService;
import com.tht.ifdatamigrator.dto.AssessmentDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Long.parseLong;
import static java.util.Objects.isNull;

@Slf4j
@Service
@AllArgsConstructor
public class AssessmentDataRestoreService {

    private final RestoreDaoService service;

    public void restore(AssessmentDTO a) {
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
        for (AssessmentDTO.AssQuestionDTO q : a.getQuestions()) {
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
}
