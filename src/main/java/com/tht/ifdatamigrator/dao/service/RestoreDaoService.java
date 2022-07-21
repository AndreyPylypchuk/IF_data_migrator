package com.tht.ifdatamigrator.dao.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tht.ifdatamigrator.dto.*;
import com.tht.ifdatamigrator.dto.AssessmentDTO.AssQuestionDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.String.join;
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
            insert into assessment(company_id, assessment_name, custom, integrity_first, assessment_version_id, from_ati)
            values (?, ?, true, true, (select assessment_version_id from assessment_version where name = ?), true);
            """;

    private static final String CREATE_ASSESSMENT_QUALITY = """
            insert into assessment_quality(assessment_id, quality_id) VALUES (?, ?);
            """;

    private static final String CREATE_ASSESSMENT_QUESTION = """
            insert into assessment_question(assessment_id, question_id, question_order, assessment_question_active)
            VALUES (?, ?, ?, 1);
            """;

    private static final String CREATE_COMPANY = """
            insert into company(company_status_id, company_name, company_owner_user_id, billing_email_address, ati_cust, ati_store)
            values (1, ?, ?, ?, ?, ?)
            """;

    private static final String CREATE_COMPANY_ASSESSMENT = """
            insert into company_assessment_version
            values (?, (select assessment_version_id from assessment_version where name = ?))
            """;

    private static final String GET_USER_ID_BY_EMAIL = """
            select user_id from "user" where user_email_address = ? limit 1
            """;

    private static final String CREATE_USER = """
            insert into "user"(user_email_address, password, ati_id) values (?, ?, ?)
            """;

    private static final String CREATE_MY_ACCOUNT_USER = """
            insert into user_myaccount(user_id, company_id, active, permission_level, access_to_jobposts, access_to_departments)
            values (?, ?, 1, ?, ?, ?)
            """;

    private static final String GET_ID = """
            select %s from %s where %s limit 1
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

    public void createAssessmentQuestion(long assId, Long questionId, AssQuestionDTO q) {
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(CREATE_ASSESSMENT_QUESTION);
            ps.setLong(1, assId);
            ps.setLong(2, questionId);
            ps.setInt(3, q.getOrder());
            return ps;
        });
    }

    public Map<String, Object> createCompany(CompanyDTO companyDTO) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_COMPANY, new String[]{"company_id"});
            ps.setString(1, companyDTO.getName());
            ps.setString(2, null);
            ps.setString(3, null);
            ps.setString(4, companyDTO.getNum());
            ps.setString(5, companyDTO.getStore());
            return ps;
        }, keyHolder);

        return keyHolder.getKeys();
    }

    public void createCompanyAssessments(long companyId, List<String> assessmentVersions) {
        template.batchUpdate(
                CREATE_COMPANY_ASSESSMENT,
                new BatchPreparedStatementSetter() {
                    @SneakyThrows
                    public void setValues(PreparedStatement ps, int i) {
                        ps.setLong(1, companyId);
                        ps.setString(2, assessmentVersions.get(i));
                    }

                    public int getBatchSize() {
                        return assessmentVersions.size();
                    }
                });
    }

    public Long getUserIdByEmail(String email) {
        try {
            return template.queryForObject(GET_USER_ID_BY_EMAIL, Long.class, email);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    public Long createUser(UserDTO u) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_USER, new String[]{"user_id"});
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPassword());
            ps.setLong(3, u.getId());
            return ps;
        }, keyHolder);

        return Long.parseLong(keyHolder.getKeys().get("user_id").toString());
    }

    public void createMyAccountUser(Long userId, long companyId, String role) {
        String level = isNull(role) ? "myaccount_viewer" : role;

        boolean hasAccess = "myaccount_admin".equals(level) || "myaccount_editor".equals(level);

        String toJobPost = hasAccess ? "all_posts" : "custom";
        String toDepart = hasAccess ? "all_departments" : "custom";

        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(CREATE_MY_ACCOUNT_USER);
            ps.setLong(1, userId);
            ps.setLong(2, companyId);
            ps.setString(3, level);
            ps.setString(4, toJobPost);
            ps.setString(5, toDepart);
            return ps;
        });
    }

    public Long getId(String table, String identifierField, Map<String, String> conditions) {
        List<String> conditionRequestParts = new ArrayList<>();
        conditions.forEach((k, v) -> conditionRequestParts.add(k + " = '" + v + "'"));
        String sql = format(GET_ID, identifierField, table, join(" and ", conditionRequestParts));
        try {
            return template.queryForObject(sql, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
