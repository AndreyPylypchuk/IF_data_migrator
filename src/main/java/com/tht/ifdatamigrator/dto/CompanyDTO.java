package com.tht.ifdatamigrator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CompanyDTO {
    private String num;
    private String store;
    private String name;
    private Long thtId;

    private List<UserDTO> users = new ArrayList<>();
    private List<AssessmentData> assessmentData = new ArrayList<>();

    @Data
    public static class AssessmentData {
        private String atiVersion;
        private String thtVersion;
        private boolean hasApplicants;
        private List<ApplicantDTO> applicants = new ArrayList<>();
    }

    public CompanyDTO(String num, String store, String name, Long thtId) {
        this.num = num;
        this.store = store;
        this.name = name;
        this.thtId = thtId;
    }
}
