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

    private List<UserDTO> users = new ArrayList<>();
    private List<AssessmentVersion> assessmentVersions = new ArrayList<>();

//    private List<VersionApplicant> versionApplicants = new ArrayList<>()

    @Data
    public static class AssessmentVersion {
        private String atiVersion;
        private String thtVersion;
    }

    public CompanyDTO(String num, String store, String name) {
        this.num = num;
        this.store = store;
        this.name = name;
    }
}
