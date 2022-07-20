package com.tht.ifdatamigrator.dao.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tht.ifdatamigrator.dto.AnswerDTO;
import com.tht.ifdatamigrator.dto.AssessmentDTO;
import com.tht.ifdatamigrator.dto.AssessmentDTO.AssQuestionDTO;
import com.tht.ifdatamigrator.dto.QuestionDTO;
import com.tht.ifdatamigrator.dto.TranslateDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class RestoreDaoService {

    @Value("${migration.restore.assessment.companyId}")
    private Long companyId;

    private static final String CREATE_QUESTION = """
            insert into question (quality_id, question_text, question_type, scored, skippable, ati_code, ati_full_code)
            values (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String CREATE_ANSWER = """
            insert into question_answer (question_id, answer_text, answer_order, answer_score, admission)
            values (?, ?, ?, ?, ?)
            """;

    private static final String CREATE_TRANSLATE = """
            insert into public.translation(translation_id, locale_code, translation_body) VALUES (?, ?, ?::json)
            """;

    private static final String CREATE_ASSESSMENT = """
            insert into assessment(company_id, assessment_name, custom, integrity_first, assessment_version_id)
            values (?, ?, true, true, (select assessment_version_id from assessment_version where name = ?));
            """;

    private static final String CREATE_ASSESSMENT_QUALITY = """
            insert into assessment_quality(assessment_id, quality_id) VALUES (?, ?);
            """;

    private static final String CREATE_ASSESSMENT_QUESTION = """
            insert into assessment_question(assessment_id, question_id, question_order, assessment_question_active)
            VALUES (?, ?, ?, 1);
            """;

    private static final String SELECT_QUESTION = """
            select question_id
            from question
            where ati_full_code = ?
            """;

    private final JdbcTemplate template;

    public Map<String, Object> createQuestion(QuestionDTO q) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_QUESTION, new String[]{"question_id", "translation_id"});
            ps.setLong(1, q.getQualityId());
            ps.setString(2, q.getText());
            ps.setString(3, q.getType());
            ps.setBoolean(4, q.getScored());
            ps.setBoolean(5, q.getSkip());
            ps.setString(6, q.getId());
            ps.setString(7, q.getCode());
            return ps;
        }, keyHolder);

        return keyHolder.getKeys();
    }

    public void createTranslation(long translateId, List<TranslateDTO> translates, String fieldName) {
        template.batchUpdate(
                CREATE_TRANSLATE,
                new BatchPreparedStatementSetter() {
                    @SneakyThrows
                    public void setValues(PreparedStatement ps, int i) {
                        String body = new ObjectMapper().writeValueAsString(
                                singletonMap(fieldName, translates.get(i).getText())
                        );

                        ps.setLong(1, translateId);
                        ps.setString(2, translates.get(i).getLocaleCode());
                        ps.setString(3, body);
                    }

                    public int getBatchSize() {
                        return translates.size();
                    }
                });
    }

    public Map<String, Object> createAnswer(AnswerDTO a, Long questionId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_ANSWER, new String[]{"translation_id"});
            ps.setLong(1, questionId);
            ps.setString(2, a.getText());
            ps.setInt(3, a.getOrder());
            ps.setDouble(4, a.getScore());
            ps.setString(5, a.getAdmission());
            return ps;
        }, keyHolder);

        return keyHolder.getKeys();
    }

    public Map<String, Object> createAssessment(AssessmentDTO a) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_ASSESSMENT, new String[]{"assessment_id", "translation_id"});
            ps.setLong(1, companyId);
            ps.setString(2, a.getName());
            ps.setString(3, a.getVersion());
            return ps;
        }, keyHolder);

        return keyHolder.getKeys();
    }

    @SneakyThrows
    public void createTranslation(long translateId, Map<String, String> translate) {
        String body = new ObjectMapper().writeValueAsString(translate);
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(CREATE_TRANSLATE);
            ps.setLong(1, translateId);
            ps.setString(2, "en");
            ps.setString(3, body);
            return ps;
        });
    }

    public void createAssessmentQuality(long assId, Integer q) {
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(CREATE_ASSESSMENT_QUALITY);
            ps.setLong(1, assId);
            ps.setLong(2, q);
            return ps;
        });
    }

    public void createAssessmentQuestion(long assId, AssQuestionDTO q) {
        try {
            Long questionId = template.queryForObject(SELECT_QUESTION, Long.class, q.getAtiFullCode());
            if (isNull(questionId)) return;
            template.update(con -> {
                PreparedStatement ps = con.prepareStatement(CREATE_ASSESSMENT_QUESTION);
                ps.setLong(1, assId);
                ps.setLong(2, questionId);
                ps.setInt(3, q.getOrder());
                return ps;
            });
        } catch (EmptyResultDataAccessException ignored){}
    }
}
