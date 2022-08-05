package com.tht.ifdatamigrator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class CompanyDTO {
    private String num;
    private String store;
    private String name;
    private String storeName;
    private Long thtId;

    private Set<UserDTO> users = new HashSet<>();
    private List<AssessmentData> assessmentData = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentData {
        private String atiVersion;
        private String thtVersion;
        private boolean hasApplicants;
        private List<ApplicantDTO> applicants = new ArrayList<>();

        public AssessmentData(String version) {
            thtVersion = version;
        }
    }

    public CompanyDTO(String num, String store, String name, String storeName, Long thtId) {
        this.num = num;
        this.store = store;
        this.name = name;
        this.storeName = storeName;
        this.thtId = thtId;
    }
}
