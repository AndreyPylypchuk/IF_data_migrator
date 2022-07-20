package com.tht.ifdatamigrator.dao.domain;

import lombok.Data;

@Data
public class Question {
    private String id;
    private String questionCode;
    private String questionDescription;
    private String scoredYesNo;
    private String dataEntryType;
    private String skipQuestionAllowed;
    private String category;
    private String langCode;
}
