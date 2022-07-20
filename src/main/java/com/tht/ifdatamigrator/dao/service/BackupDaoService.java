package com.tht.ifdatamigrator.dao.service;

import com.tht.ifdatamigrator.dao.domain.*;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tht.ifdatamigrator.Const.VERSIONS;

@Service
@AllArgsConstructor
public class BackupDaoService {

    private static final String QUESTIONS = """
            select q.QuestionCode,
                   q.QuestionDescription,
                   q.ScoredYesNo,
                   q.DataEntryType,
                   q.SkipQuestionAllowed,
                   qc.Category,
                   qc.LangCode
            from atiQuestions q
                     join (select distinct Questioncode, Category, LangCode
                           from atiTestQuestions
                           where Testcode in (:versions)) qc
                          on q.QuestionCode = qc.Questioncode;
            """;

    private static final String ANSWERS = """
            select * from atiAdmissions where QuestionCode like '%' + :code
            """;

    private static final String ASSESSMENTS = """
            select TestCode, TestName from atiTestNames where TestCode in (:versions)
            """;

    private static final String ASSESSMENT_CATEGORIES = """
            select distinct Category
            from atiTestQuestions
            where Testcode = :code
              and LangCode = 'EN'
            """;

    private static final String ASSESSMENT_QUESTIONS = """
            select Questioncode, SerialOrder
            from atiTestQuestions
            where Testcode = :code
              and LangCode = 'EN'
            """;

    private static final String COMPANIES = """
            select c.CustNum, c.CustName, s.City, s.state
            from atiCustomer c
                     join atiAllStores s on c.CustNum = s.Cust#
            """;

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public List<Question> getQuestions() {
        SqlParameterSource parameters = new MapSqlParameterSource("versions", VERSIONS);
        return namedTemplate.query(QUESTIONS, parameters, new BeanPropertyRowMapper<>(Question.class));
    }

    public List<Answer> getAnswers(String code) {
        SqlParameterSource parameters = new MapSqlParameterSource("code", code);
        return namedTemplate.query(ANSWERS, parameters, new BeanPropertyRowMapper<>(Answer.class));
    }

    public List<Assessment> getAssessments() {
        SqlParameterSource parameters = new MapSqlParameterSource("versions", VERSIONS);
        return namedTemplate.query(ASSESSMENTS, parameters, new BeanPropertyRowMapper<>(Assessment.class));
    }

    public List<String> getAssessmentCategories(String testCode) {
        SqlParameterSource parameters = new MapSqlParameterSource("code", testCode);
        return namedTemplate.queryForList(ASSESSMENT_CATEGORIES, parameters, String.class);
    }

    public List<AssessmentQuestion> getAssessmentQuestions(String testCode) {
        SqlParameterSource parameters = new MapSqlParameterSource("code", testCode);
        return namedTemplate.query(ASSESSMENT_QUESTIONS, parameters, new BeanPropertyRowMapper<>(AssessmentQuestion.class));
    }

    public List<Company> getCompanies() {
        return template.query(COMPANIES, new BeanPropertyRowMapper<>(Company.class));
    }
}
