package com.tht.ifdatamigrator.dto;

import com.tht.ifdatamigrator.dao.domain.AssessmentQuestion;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssessmentDTO {
    private String name;
    private String version;
    private List<Integer> qualities = new ArrayList<>();
    private List<AssQuestionDTO> questions = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class AssQuestionDTO {
        private String atiFullCode;
        private Integer order;

        public AssQuestionDTO(AssessmentQuestion question) {
            atiFullCode = question.getQuestioncode();
            order = question.getSerialOrder();
        }
    }
}
