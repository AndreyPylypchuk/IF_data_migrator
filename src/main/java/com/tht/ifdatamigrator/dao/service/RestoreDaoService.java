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
import static java.sql.Types.INTEGER;
import static java.util.Collections.singletonMap;
import static java.util.Objects.nonNull;

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
            insert into "user"(user_status_id, user_email_address, ati_id, from_ati, ati_notified, welcome_message_viewed) values (1, ?, ?, true, false, false)
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
    public void createAssTranslation(long translateId, Map<String, String> translate, String locale) {
        String body = new ObjectMapper().writeValueAsString(translate);
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(CREATE_TRANSLATE);
            ps.setLong(1, translateId);
            ps.setString(2, locale);
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

    public Map<String, Object> createCompany(CompanyDTO companyDTO, Long adminId, String adminEmail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_COMPANY, new String[]{"company_id"});
            ps.setString(1, companyDTO.getName());
            ps.setLong(2, adminId);
            ps.setString(3, adminEmail);
            ps.setString(4, companyDTO.getNum());
            ps.setString(5, companyDTO.getStore());
            return ps;
        }, keyHolder);

        return keyHolder.getKeys();
    }

    public void createCompanyAssessment(long companyId, String assessmentVersion) {
        try {
            template.update(con -> {
                PreparedStatement ps = con.prepareStatement(CREATE_COMPANY_ASSESSMENT);
                ps.setLong(1, companyId);
                ps.setString(2, assessmentVersion);
                return ps;
            });
        } catch (Exception ignored) {
        }
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
            if (nonNull(u.getId()))
                ps.setInt(2, u.getId());
            else
                ps.setNull(2, INTEGER);
            return ps;
        }, keyHolder);

        return Long.parseLong(keyHolder.getKeys().get("user_id").toString());
    }

    public void createMyAccountUser(Long userId, long companyId, String role) {
        String toJobPost = "all_posts";
        String toDepart = "all_departments";

        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(CREATE_MY_ACCOUNT_USER);
            ps.setLong(1, userId);
            ps.setLong(2, companyId);
            ps.setString(3, role);
            ps.setString(4, toJobPost);
            ps.setString(5, toDepart);
            return ps;
        });
    }

    public Long getId(String table, String identifierField, Map<String, Object> conditions) {
        List<String> conditionRequestParts = new ArrayList<>();
        conditions.forEach((k, v) -> {
            if (v == null)
                conditionRequestParts.add(k + " is null");
            else if (v instanceof String vStr) {
                String valueStr = vStr.replaceAll("'", "''");
                conditionRequestParts.add(k + " = '" + valueStr + "'");
            } else
                conditionRequestParts.add(k + " = " + v);
        });
        String sql = format(GET_ID, identifierField, table, join(" and ", conditionRequestParts));
        try {
            return template.queryForObject(sql, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Long getCompanyJobpostingStatusId(Long comJobpostId) {
        String sql = """
                select company_jobposting_status_id
                from company_jobposting_status
                where company_jobposting_id = ?
                and status_type = 'assessment'
                limit 1
                """;
        return template.queryForObject(sql, Long.class, comJobpostId);
    }

    public Long getAssessment(String assVersion) {
        String sql = """
                select assessment_id
                from assessment a
                         left join assessment_version v on a.assessment_version_id = v.assessment_version_id
                where a.from_ati = true
                  and v.name = ? limit 1
                """;
        return template.queryForObject(sql, Long.class, assVersion);
    }

    public void createCompanyJobpostStatusAss(Long companyJobpostingStatusId, Long assId) {
        String sql = """
                insert into company_jobposting_status_assessment(company_jobposting_status_id, assessment_id)
                values (?, ?)
                """;
        template.update(sql, companyJobpostingStatusId, assId);
    }

    public Long getAdminId(Long companyId) {
        String sql = """
                select company_owner_user_id
                from company
                where company_id = ?
                """;
        return template.queryForObject(sql, Long.class, companyId);
    }

    public void createCompanyStatus(Long companyId) {
        Long id = getId("status", "status_id", singletonMap("company_id", companyId));
        if (nonNull(id))
            return;

        String sql = """
                insert into status(status_name, status_type, email_template_id, company_id, status_order, color)
                select status_name, status_type, email_template_id, ?, status_order, color
                from status
                where company_id = 0
                """;

        template.update(sql, companyId);
    }

    public void createCompanyEmailTemplate(Long companyId) {
        Long id = getId("email_template", "email_template_id", singletonMap("company_id", companyId));
        if (nonNull(id))
            return;

        String sql = """
                insert into public.email_template(template_type,
                                                  template_name,
                                                  company_id,
                                                  name,
                                                  subject,
                                                  body,
                                                  plaintext,
                                                  email_created_by)
                select template_type,
                       template_name,
                       ?,
                       name,
                       subject,
                       body,
                       plaintext,
                       email_created_by
                from email_template
                where company_id = 0;
                """;

        template.update(sql, companyId);
    }

    public void createCompanyReference(Long companyId) {
        Long id = getId("reference_contact_settings", "company_id", singletonMap("company_id", companyId));
        if (nonNull(id))
            return;

        String sql = """
                insert into reference_contact_settings(time_interval_type,
                                                       time_interval_frequency,
                                                       first_priority,
                                                       second_priority,
                                                       third_priority,
                                                       do_not_contact_before,
                                                       do_not_contact_after,
                                                       phone_introduction,
                                                       sms_introduction,
                                                       company_id)
                select time_interval_type,
                       time_interval_frequency,
                       first_priority,
                       second_priority,
                       third_priority,
                       do_not_contact_before,
                       do_not_contact_after,
                       phone_introduction,
                       sms_introduction,
                       ?
                from reference_contact_settings
                where company_id = 0
                """;

        template.update(sql, companyId);
    }
}
