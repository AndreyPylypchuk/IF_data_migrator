package com.tht.ifdatamigrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tht.ifdatamigrator.dao.domain.*;
import com.tht.ifdatamigrator.dao.service.BackupDaoService;
import com.tht.ifdatamigrator.dto.*;
import com.tht.ifdatamigrator.dto.AssessmentDTO.AssQuestionDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.lang.Integer.parseInt;
import static java.nio.file.Files.writeString;
import static java.nio.file.Path.of;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    @Value("${migration.scopes}")
    private List<String> scopes;

    private final BackupDaoService service;

    @SneakyThrows
    public void backup() {
        log.info("Backup started");

        BackupDTO result = new BackupDTO();

        if (scopes.contains("questionAnswerData")) result.setQuestionAnswerData(backupQuestionAnswerData());
        if (scopes.contains("assessmentData")) result.setAssessmentData(backupAssessmentData());
        if (scopes.contains("company")) result.setCompanies(backupCompanyData());

        writeString(
                of("backup.json"),
                new ObjectMapper().writeValueAsString(result), CREATE, TRUNCATE_EXISTING
        );

        log.info("Backup finished");
    }

    private List<CompanyDTO> backupCompanyData() {
        log.info("Company data extracting...");

        List<Company> companies = service.getCompanies();

        Map<String, List<Company>> numCompanies = companies.stream()
                .collect(groupingBy(Company::getCustNum));

        return null;
    }

    private List<AssessmentDTO> backupAssessmentData() {
        log.info("Assessment data extracting...");
        List<Assessment> assessments = service.getAssessments();
        List<AssessmentDTO> dtos = assessments.stream()
                .map(a -> {
                    AssessmentDTO dto = new AssessmentDTO();
                    dto.setName(a.getTestName().trim());
                    dto.setVersion(a.getTestCode().trim().replaceAll("-", ""));

                    List<String> categories = service.getAssessmentCategories(a.getTestCode());
                    dto.setQualities(mapQuality(categories));

                    List<AssessmentQuestion> questions = service.getAssessmentQuestions(a.getTestCode());
                    dto.setQuestions(questions.stream().map(AssQuestionDTO::new).collect(toList()));

                    return dto;
                }).collect(toList());
        return dtos;
    }

    private List<QuestionDTO> backupQuestionAnswerData() {
        log.info("Question/Answer extracting...");
        List<Question> questions = service.getQuestions();

        Map<String, List<Question>> questionMap = questions.stream()
                .peek(q -> q.setId(q.getQuestionCode().substring(2)))
                .collect(groupingBy(Question::getId));

        List<QuestionDTO> questionDTOs = questionMap.entrySet().stream()
                .map(entry -> {
                    QuestionDTO dto = new QuestionDTO();
                    dto.setId(entry.getKey());

                    Question enQuestion = entry.getValue().stream()
                            .filter(q -> q.getLangCode().equals("EN"))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Not found EN question version"));

                    dto.setCode(enQuestion.getQuestionCode().trim());
                    dto.setText(enQuestion.getQuestionDescription().trim());

                    dto.setType(
                            "Multiple".equalsIgnoreCase(enQuestion.getDataEntryType().trim()) ?
                                    "multiple_choice" :
                                    "regular"
                    );

                    dto.setScored("yes".equalsIgnoreCase(enQuestion.getScoredYesNo().trim()));
                    dto.setQualityId(mapQuality(enQuestion.getCategory()));
                    dto.setSkip("yes".equalsIgnoreCase(enQuestion.getSkipQuestionAllowed().trim()));
                    dto.setTranslates(mapQuestionTranslates(entry.getValue()));

                    return dto;
                })
                .filter(dto -> nonNull(dto.getQualityId()))
                .collect(toList());

        questionDTOs.forEach(q -> {
            log.info("Answer extracting for question {} ...", q.getId());
            List<Answer> answers = service.getAnswers(q.getId());
            Map<String, List<Answer>> answerOrderMap = answers.stream()
                    .collect(groupingBy(Answer::getAnswerCode));

            q.setAnswers(
                    answerOrderMap.values().stream()
                            .map(orderAnswers -> {
                                AnswerDTO dto = new AnswerDTO();

                                Answer enAnswer = orderAnswers.stream()
                                        .filter(a -> a.getLangCode().equals("EN"))
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException("Not found EN answer version"));

                                dto.setText(enAnswer.getAnswerDescription().trim());
                                dto.setAdmission(enAnswer.getDispositionorscore().trim());
                                dto.setOrder(parseInt(enAnswer.getAnswerCode()));
                                dto.setScore(enAnswer.getAnswerValue().doubleValue());
                                dto.setTranslates(mapAnswerTranslates(orderAnswers));

                                return dto;
                            }).collect(toList())
            );
        });

        return questionDTOs;
    }

    private List<Integer> mapQuality(List<String> categories) {
        Set<Integer> results = new HashSet<>();
        categories.forEach(c -> results.add(mapQuality(c)));
        return results.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private List<TranslateDTO> mapAnswerTranslates(List<Answer> value) {
        return value.stream()
                .map(v -> {
                    TranslateDTO dto = new TranslateDTO();
                    dto.setText(v.getAnswerDescription().trim());
                    dto.setLocaleCode(v.getLangCode().trim().toLowerCase());
                    return dto;
                }).collect(toList());
    }

    private List<TranslateDTO> mapQuestionTranslates(List<Question> value) {
        return value.stream()
                .map(v -> {
                    TranslateDTO dto = new TranslateDTO();
                    dto.setText(v.getQuestionDescription().trim());
                    dto.setLocaleCode(v.getLangCode().trim().toLowerCase());
                    return dto;
                }).collect(toList());
    }

    private Integer mapQuality(String category) {
        if (category.endsWith("THEFT")) return 1338;
        if (category.endsWith("FAKES")) return 1339;
        if (category.endsWith("DRUGS")) return 1340;
        if (category.endsWith("HOSTL")) return 1341;
        if (category.endsWith("FILLR")) return 1342;
        if (category.endsWith("SAFETY")) return 1344;
        if (category.endsWith("HEALTH")) return 1345;
        if (category.endsWith("INDUSTRY")) return 1346;
        if (category.endsWith("SAFTEY")) return 1344;
        return null;
    }
}
