package com.tht.ifdatamigrator.service.restore;

import com.tht.ifdatamigrator.dao.service.RestoreDaoService;
import com.tht.ifdatamigrator.dto.AnswerDTO;
import com.tht.ifdatamigrator.dto.QuestionDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Long.parseLong;
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Slf4j
@Service
@AllArgsConstructor
public class QuestionAnswerRestoreService {

    private final RestoreDaoService service;

    public void restore(QuestionDTO q) {
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
