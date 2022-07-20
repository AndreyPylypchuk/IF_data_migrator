package com.tht.ifdatamigrator.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyDTO {
    private String num;
    private String name;
    private List<Store> stores = new ArrayList<>();

    @Data
    public static class Store {
        private String city;
        private String state;
    }
}
