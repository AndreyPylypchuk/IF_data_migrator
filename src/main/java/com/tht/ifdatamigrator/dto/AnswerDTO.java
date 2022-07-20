package com.tht.ifdatamigrator.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AnswerDTO {
    private Integer order;
    private Double score;
    private String text;
    private String admission;

    private List<TranslateDTO> translates = new ArrayList<>();
}
