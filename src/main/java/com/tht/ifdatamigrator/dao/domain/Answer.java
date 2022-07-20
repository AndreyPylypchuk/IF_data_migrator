package com.tht.ifdatamigrator.dao.domain;

import lombok.Data;

@Data
public class Answer {
    private String answerCode;
    private String langCode;
    private String answerDescription;
    private String dispositionorscore;
    private Integer answerValue;
}
