package com.tht.ifdatamigrator.service.backup;

import com.tht.ifdatamigrator.dao.domain.Answer;
import com.tht.ifdatamigrator.dao.domain.Question;
import com.tht.ifdatamigrator.dao.service.BackupDaoService;
import com.tht.ifdatamigrator.dto.AnswerDTO;
import com.tht.ifdatamigrator.dto.QuestionDTO;
import com.tht.ifdatamigrator.dto.TranslateDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.tht.ifdatamigrator.service.backup.BackupUtils.mapQuality;
import static java.lang.Integer.parseInt;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@AllArgsConstructor
public class QuestionAnswerBackupService {

    private final BackupDaoService service;

    public List<QuestionDTO> backupQuestionAnswerData() {
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
}
