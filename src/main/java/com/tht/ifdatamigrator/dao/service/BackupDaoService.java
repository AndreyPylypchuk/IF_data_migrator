package com.tht.ifdatamigrator.dao.service;

import com.tht.ifdatamigrator.dao.domain.*;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.tht.ifdatamigrator.Const.MIGRATED_COMPANIES;
import static com.tht.ifdatamigrator.Const.VERSIONS;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class BackupDaoService {

    private final NamedParameterJdbcTemplate namedTemplate;

    public List<Question> getQuestions() {
        String sql = """
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

        SqlParameterSource parameters = new MapSqlParameterSource("versions", VERSIONS);
        return namedTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(Question.class));
    }

    public List<Answer> getAnswers(String code) {
        String sql = """
                select * from atiAdmissions where QuestionCode like '%' + :code
                """;

        SqlParameterSource parameters = new MapSqlParameterSource("code", code);
        return namedTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(Answer.class));
    }

    public List<Assessment> getAssessments() {
        String sql = """
                select TestCode, TestName from atiTestNames where TestCode in (:versions)
                """;

        SqlParameterSource parameters = new MapSqlParameterSource("versions", VERSIONS);
        return namedTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(Assessment.class));
    }

    public List<String> getAssessmentCategories(String testCode) {
        String sql = """
                select distinct Category
                from atiTestQuestions
                where Testcode = :code
                  and LangCode = 'EN'
                """;

        SqlParameterSource parameters = new MapSqlParameterSource("code", testCode);
        return namedTemplate.queryForList(sql, parameters, String.class);
    }

    public List<AssessmentQuestion> getAssessmentQuestions(String testCode) {
        String sql = """
                select Questioncode, SerialOrder
                from atiTestQuestions
                where Testcode = :code
                  and LangCode = 'EN'
                """;

        SqlParameterSource parameters = new MapSqlParameterSource("code", testCode);
        return namedTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(AssessmentQuestion.class));
    }

    public List<Company> getCompanies() {
        String sql = """
                select c.CustNum, c.CustName, s.StoreNum as store, s.CustName as StoreName
                from atiCustomer c
                         left join (select * from atiStore where Active = 'True') s on c.CustNum = s.CustNum
                where c.CustNum in (:companies)
                """;

        SqlParameterSource parameters = new MapSqlParameterSource("companies", MIGRATED_COMPANIES.keySet());
        return namedTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(Company.class));
    }

    public List<User> getUsers(String cusNum, String storeNum) {
        String sql = """
                select ucc.UserCredentialId, EmailAddress, RoleID
                from UserCredentials uc
                join UserCredentialCoverage ucc on uc.UserCredentialId = ucc.UserCredentialId
                where uc.IsActive = 1 and ucc.CustomerNumber = :cut
                """;

        if (nonNull(storeNum))
            sql += " and ucc.StoreNumber = :store";
        SqlParameterSource parameters = new MapSqlParameterSource("cut", cusNum).
                addValue("store", storeNum);
        return namedTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(User.class));
    }

    public List<String> getCompanyAssessment(String cusNum, String storeNum) {
        String sql = """
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
                  and at2.TestCode in (:versions)
                and ucc.CustomerNumber = :cut
                """;

        if (nonNull(storeNum))
            sql += " and ucc.StoreNumber = :store";
        SqlParameterSource parameters = new MapSqlParameterSource("cut", cusNum)
                .addValue("store", storeNum)
                .addValue("versions", VERSIONS);
        return namedTemplate.queryForList(sql, parameters, String.class);
    }

    public List<Map<String, Object>> getApplicants(String num, String store, String version) {
        String sql = """
                select *
                from atiScores
                where Cust# = :cust
                  and Test = :version
                  and year(getdate()) - year(TestDate) <= 5
                """;
        if (nonNull(store))
            sql += " and Store# = :store";

        SqlParameterSource parameters = new MapSqlParameterSource("cust", num).
                addValue("store", store)
                .addValue("version", version);

        return namedTemplate.queryForList(sql, parameters);
    }
}
