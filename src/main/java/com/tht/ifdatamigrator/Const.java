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

    public static final Map<String, CompanyStaticData> MIGRATED_COMPANIES = new HashMap<>() {{
        put("2097", new CompanyStaticData());
        put("2148", new CompanyStaticData());
    }};

    public static final List<String> IMPACTS = of(
            "Never called in late or absent to the last job.",
            "Rarely called in late or absent to the last job.",
            "Sometimes called in late or absent to the last job.",
            "Frequently called in late or absent to the last job.",
            "Not at all flexible with when and where they work.",
            "Somewhat flexible with when and where they work.",
            "Quite flexible with when and where they work.",
            "Very flexible with when and where they work.",
            "Very slow to learn a job in a new environment.",
            "Somewhat slow to learn a job in a new environment.",
            "Somewhat quickly learns a job in a new environment.",
            "Very quickly learns a job in a new environment.",
            "Not familiar with POS (point-of-sale) systems.",
            "Somewhat familiar with POS (point-of-sale) systems.",
            "Fairly familiar with POS (point-of-sale) systems.",
            "Very familiar with POS (point-of-sale) systems.",
            "Has no experience working in the hospitality or food service industry.",
            "Has some, but less than 2 years, of experience working in the hospitality or food service industry.",
            "Has 3-5 years of experience working in the hospitality or food service industry.",
            "Has more than 5 years of experience working in the hospitality or food service industry.",
            "Does not enjoy solving problems for customers.",
            "Somewhat enjoys solving problems for customers.",
            "Enjoys solving problems quite a bit for customers.",
            "Very much enjoys solving problems for customers.",
            "Has no reportable accidents in the past 5 years.",
            "Has 1-2 reportable accidents in the past 5 years.",
            "Has 3-5 reportable accidents in the past 5 years.",
            "Has more than 5 reportable accidents in the past 5 years.",
            "Has no driving infractions or violations in the past 5 years.",
            "Has 1-2 driving infractions or violations in the past 5 years.",
            "Has 3-5 driving infractions or violations in the past 5 years.",
            "Has more than 5 driving infractions or violations in the past 5 years.",
            "Does not have a a valid CDL.",
            "Has had a CDL for less than 5 years.",
            "Has had a CDL for 5-10 years.",
            "Has had a CDL for more than 10 years.",
            "Has not operated heavy equipment or machinery.",
            "Has some, but less than 2 years experience operating heavy equipment or machinery.",
            "Has 3-5 years experience operating heavy equipment or machinery.",
            "Has more than 5 years of experience operating heavy equipment or machinery.",
            "Has no experience working in a factory or manufacturing facility.",
            "Has some, but less than 2 years, of experience working in a factory or manufacturing facility.",
            "Has 3-5 years of experience working in a factory or manufacturing facility.",
            "Has more than 5 years of experience working in a factory or manufacturing facility.",
            "Has no experience working on an assembly line.",
            "Has some, but less than 2 years, of experience working on an assembly line.",
            "Has 3-5 years of experience working on an assembly line.",
            "Has more than 5 years of experience working on an assembly line.",
            "Does not enjoy working with customers.",
            "Somewhat enjoys working with customers.",
            "Enjoys working with customers quite a bit.",
            "Very much enjoys working with customers.",
            "Not familiar with POS (point-of-sale) systems.",
            "Somewhat familiar with POS (point-of-sale) systems.",
            "Fairly familiar with POS (point-of-sale) systems.",
            "Very familiar with POS (point-of-sale) systems.",
            "Has no experience working in retail.",
            "Has some, but less than 2 years, of experience working in retail.",
            "Has 3-5 years of experience working in retail.",
            "Has more than 5 years of experience working in retail.",
            "Always adapts to new situations easily.",
            "Frequently adapts to new situations easily.",
            "Sometimes adapts to new situations easily.",
            "Does not adapt to new situations easily.",
            "Not at all flexible with when and where they work.",
            "Somewhat flexible with when and where they work.",
            "Quite flexible with when and where they work.",
            "Very flexible with when and where they work.",
            "Never called in late or absent to the last job.",
            "Rarely called in late or absent to the last job.",
            "Sometimes called in late or absent to the last job.",
            "Frequently called in late or absent to the last job.");
}
