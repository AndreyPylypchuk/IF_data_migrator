package com.tht.ifdatamigrator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStaticData {
    private Long thtId;
    private Map<String, String> users = new HashMap<>();
}
