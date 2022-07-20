package com.tht.ifdatamigrator.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BackupDTO {
    private List<QuestionDTO> questionAnswerData = new ArrayList<>();
    private List<AssessmentDTO> assessmentData = new ArrayList<>();
    private List<CompanyDTO> companies = new ArrayList<>();
}
