package com.tht.ifdatamigrator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.List.of;

public final class Const {
    private Const() {
    }

    public static final List<String> VERSIONS = of(
            "MES",
            "MIT-54M5V2",
            "MIT-CAN45V2MT",
            "MIT-CAN45V2",
            "MIT-HC63V3",
            "MIT-RIMA54V2",
            "MIT-54M5RV2",
            "MIT-54M5SV2",
            "MIT-54M5HV2",
            "MIT-54M5MV2",
            "MIT-54M5TV2",
            "MIT-54M5NFV2"
    );

    public static final Map<String, CompanyStaticData> MIGRATED_COMPANIES = new HashMap<>() {{
        put("2048", new CompanyStaticData(null, singletonMap("cityhall21@cityofmomence.com", "myaccount_admin")));
        put("2132", new CompanyStaticData(null, singletonMap("emailadmin@carlylelake.com", "myaccount_admin")));
    }};
}
