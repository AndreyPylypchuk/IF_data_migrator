package com.tht.ifdatamigrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tht.ifdatamigrator.dto.BackupDTO;
import com.tht.ifdatamigrator.service.backup.AssessmentDataBackupService;
import com.tht.ifdatamigrator.service.backup.CompanyDataBackupService;
import com.tht.ifdatamigrator.service.backup.QuestionAnswerBackupService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.nio.file.Files.writeString;
import static java.nio.file.Path.of;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    @Value("${migration.scopes}")
    private List<String> scopes;

    private final QuestionAnswerBackupService questionAnswerBackupService;
    private final AssessmentDataBackupService assessmentDataBackupService;
    private final CompanyDataBackupService companyDataBackupService;

    @SneakyThrows
    public void backup() {
        log.info("Backup started");

        BackupDTO result = new BackupDTO();

        if (scopes.contains("questionAnswerData"))
            result.setQuestionAnswerData(questionAnswerBackupService.backupQuestionAnswerData());

        if (scopes.contains("assessmentData"))
            result.setAssessmentData(assessmentDataBackupService.backupAssessmentData());

        if (scopes.contains("companyData") || scopes.contains("companyApplicant"))
            result.setCompanyData(companyDataBackupService.backupCompanyData());

        writeString(
                of("backup.json"),
                new ObjectMapper().writeValueAsString(result), CREATE, TRUNCATE_EXISTING
        );

        log.info("Backup finished");
    }
}
