package com.tht.ifdatamigrator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStaticData {
    private Long thtId;
    private Map<String, String> users = new HashMap<>();
    private List<String> stores = new ArrayList<>();

    public CompanyStaticData(List<String> stores) {
        this.stores = stores;
    }
}
