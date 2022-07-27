package com.tht.ifdatamigrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateJobpostResponse {
    @JsonProperty("company_jobposting_id")
    private Long companyJobpostingId;
    private List<Status> statuses = new ArrayList<>();

    @Data
    public static class Status {
        @JsonProperty("company_jobposting_status_id")
        private Long companyJobpostingStatusId;
        @JsonProperty("status_type")
        private String statusType;
    }
}
