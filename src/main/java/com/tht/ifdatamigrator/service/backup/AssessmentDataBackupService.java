package com.tht.ifdatamigrator.service.backup;

import com.tht.ifdatamigrator.dao.domain.Assessment;
import com.tht.ifdatamigrator.dao.domain.AssessmentQuestion;
import com.tht.ifdatamigrator.dao.service.BackupDaoService;
import com.tht.ifdatamigrator.dto.AssessmentDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@AllArgsConstructor
public class AssessmentDataBackupService {

    private final BackupDaoService service;

    public List<AssessmentDTO> backupAssessmentData() {
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
                    dto.setQuestions(questions.stream().map(AssessmentDTO.AssQuestionDTO::new).collect(toList()));

                    return dto;
                }).collect(toList());
        return dtos;
    }

    private List<Integer> mapQuality(List<String> categories) {
        Set<Integer> results = new HashSet<>();
        categories.forEach(c -> results.add(BackupUtils.mapQuality(c)));
        return results.stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
