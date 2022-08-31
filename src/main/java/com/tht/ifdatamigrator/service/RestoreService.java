package com.tht.ifdatamigrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tht.ifdatamigrator.dto.BackupDTO;
import com.tht.ifdatamigrator.service.restore.ApplicantRestoreService;
import com.tht.ifdatamigrator.service.restore.AssessmentDataRestoreService;
import com.tht.ifdatamigrator.service.restore.CompanyDataRestoreService;
import com.tht.ifdatamigrator.service.restore.QuestionAnswerRestoreService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreService {

    @Value("${migration.scopes}")
    private List<String> scopes;

    private final ObjectMapper mapper;
    private final QuestionAnswerRestoreService questionAnswerRestoreService;
    private final AssessmentDataRestoreService assessmentDataRestoreService;
    private final CompanyDataRestoreService companyDataRestoreService;
    private final ApplicantRestoreService applicantRestoreService;

    @SneakyThrows
    public void restore() {
        log.info("Restoring started");

        BackupDTO data = mapper.readValue(new File("backup.json"), BackupDTO.class);

        if (scopes.contains("questionAnswerData"))
            data.getQuestionAnswerData().forEach(questionAnswerRestoreService::restore);

        if (scopes.contains("assessmentData"))
            data.getAssessmentData().forEach(assessmentDataRestoreService::restore);

        if (scopes.contains("companyData"))
            data.getCompanyData().forEach(companyDataRestoreService::restore);

        if (scopes.contains("companyApplicant"))
            data.getCompanyData().forEach(applicantRestoreService::restore);

        log.info("Restoring finished");
    }
}
