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

import static com.tht.ifdatamigrator.Const.COMPANY_NUMS;
import static com.tht.ifdatamigrator.Const.VERSIONS;
import static java.util.Objects.nonNull;

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
            select c.CustNum, c.CustName, s.Store# as store, s.StoreName
            from atiCustomer c
                     left join (select * from atiAllStores where Active = 'True') s on c.CustNum = s.Cust#
            where c.CustNum in (:companies)
            """;

    private static final String COMPANY_USERS = """
            select ucc.UserCredentialId, EmailAddress, RoleID
            from UserCredentials uc
            join UserCredentialCoverage ucc on uc.UserCredentialId = ucc.UserCredentialId
            where uc.IsActive = 1 and ucc.CustomerNumber = :cut
            """;

    private static final String COMPANY_ASSESSMENT = """
            select distinct at2.TestCode
            from TescorNetBiz.dbo.UserCredentialCoverage ucc
                     inner join TescorNetBiz.dbo.UserCredentials uc
                                on ucc.UserCredentialId = uc.UserCredentialId
                     inner join TescorNetBiz.dbo.UniqueUrls uu
                                on uu.Applicant_Username = uc.Username
                     inner join TescorNetBiz.dbo.atiTestsAssignedToUsers atatu
                                on CONCAT(ucc.CustomerNumber, ucc.StoreNumber, 'U') = atatu.Username
                     inner join TescorNetBiz.dbo.atiTests at2
                                on at2.TestID = atatu.TestID
            where uc.RoleID = 4
              and uc.IsActive = 1
            and ucc.CustomerNumber = :cut
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
        SqlParameterSource parameters = new MapSqlParameterSource("companies", COMPANY_NUMS);
        return namedTemplate.query(COMPANIES, parameters, new BeanPropertyRowMapper<>(Company.class));
    }

    public List<User> getUsers(String cusNum, String storeNum) {
        String sql = COMPANY_USERS;
        if (nonNull(storeNum))
            sql += " and ucc.StoreNumber = :store";
        SqlParameterSource parameters = new MapSqlParameterSource("cut", cusNum).
                addValue("store", storeNum);
        return namedTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(User.class));
    }

    public List<String> getCompanyAssessment(String cusNum, String storeNum) {
        String sql = COMPANY_ASSESSMENT;
        if (nonNull(storeNum))
            sql += " and ucc.StoreNumber = :store";
        SqlParameterSource parameters = new MapSqlParameterSource("cut", cusNum).
                addValue("store", storeNum);
        return namedTemplate.queryForList(sql, parameters, String.class);
    }
}
