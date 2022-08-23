package com.tht.ifdatamigrator.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class ApplicantDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private String faking;
    private String hostility;
    private String drugs;
    private String theft;

    private String result;

    private LocalDateTime testDate;
    private LocalDateTime testStart;

    private String businessImpact;
    private String disclosures;

    private Map<Integer, Integer> questionAnswer = new HashMap<>();
}
