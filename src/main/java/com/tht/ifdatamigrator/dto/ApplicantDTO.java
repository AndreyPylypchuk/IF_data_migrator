package com.tht.ifdatamigrator.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class ApplicantDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private String faking;
    private String hostility;
    private String theftResult;
    private String fakingResult;
    private String drugsResult;
    private String hostilityResult;
    private String result;
    private LocalDateTime assessmentDate;

    private Map<Integer, Integer> questionAnswer = new HashMap<>();
}
