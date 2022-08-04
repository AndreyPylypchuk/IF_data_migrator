package com.tht.ifdatamigrator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static final Map<String, Long> MIGRATED_COMPANIES = new HashMap<>() {{
        put("0043", null);
        put("0162", null);
        put("0283", null);
        put("0336", null);
        put("0529", null);
        put("0743", null);
        put("0783", null);
        put("0855", null);
        put("0875", null);
        put("0961", null);
        put("0993", null);
        put("1099", null);
        put("1117", null);
        put("1148", null);
        put("1176", null);
        put("1182", null);
        put("1220", 1480L);
        put("1240", null);
        put("1242", null);
        put("1256", null);
        put("1292", null);
        put("1297", null);
        put("1310", null);
        put("1318", null);
        put("1338", null);
        put("1356", null);
        put("1387", null);
        put("1466", null);
        put("1498", null);
        put("1515", null);
        put("1530", null);
        put("1541", null);
        put("1557", null);
        put("1581", null);
        put("1592", null);
        put("1608", null);
        put("1662", null);
        put("1678", null);
        put("1714", null);
        put("1735", null);
        put("1770", null);
        put("1792", null);
        put("1795", null);
        put("1815", null);
        put("1823", null);
        put("1874", null);
        put("1889", null);
        put("1896", null);
        put("1915", null);
        put("1936", null);
        put("1937", null);
        put("1941", null);
        put("1942", null);
        put("1950", null);
        put("1966", null);
        put("1970", null);
        put("1973", null);
        put("1984", null);
        put("1985", null);
        put("1990", null);
        put("2010", null);
        put("2015", null);
        put("2016", null);
        put("2022", 1507L);
        put("2025", 1505L);
        put("2034", null);
        put("2036", null);
        put("2037", 1474L);
        put("2047", null);
        put("2050", null);
        put("2054", null);
        put("2055", null);
        put("2057", null);
        put("2078", null);
        put("2083", null);
        put("2085", null);
        put("2096", null);
        put("2097", 1440L);
        put("2098", null);
        put("2115", null);
        put("2116", null);
        put("2138", null);
        put("2142", 1477L);
        put("2146", 1516L);
        put("2148", null);
        put("2149", null);
        put("3370", null);
    }};
}
