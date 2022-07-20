package com.tht.ifdatamigrator.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionDTO {
    private String id;
    private String code;
    private String text;
    private Boolean scored;
    private String type;
    private Boolean skip;
    private Integer qualityId;
    private List<AnswerDTO> answers = new ArrayList<>();
    private List<TranslateDTO> translates = new ArrayList<>();
}
