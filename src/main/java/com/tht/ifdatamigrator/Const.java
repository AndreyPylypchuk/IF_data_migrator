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

    public static final List<String> COMPANY_NAME_LIST = of(
            "Cambrian Homecare",
            "Crystal Finishing Systems, Inc.",
            "Berrett Pest Control Arizona, Inc. dba Blue Sky Pest Control",
            "Buzzi Unicem USA Inc.",
            "P & S Construction Co., Inc. / PASCON, LLC",
            "Thornapple Manor",
            "Varra Companies Inc",
            "Vita-Herb Nutriceuticals, Inc.",
            "Down to Earth Compliance",
            "Elite Lighting Corp.",
            "K&K Electric, Inc.",
            "Linn Gear Company",
            "RAPAD Drilling & Well Service, Inc.",
            "Richdale Management, Inc.",
            "Telling Industries, LLC",
            "Warehouse Specialists, Inc.",
            "Waste Eliminator, Inc.",
            "Bruns Construction Enterprises Inc",
            "Charter Schools of San Diego",
            "Down River Cleaning Srvc., Inc",
            "Dunmire Development Corporation dba Pueblo Wood Pr",
            "Gary Amoth Trucking Inc",
            "HJ Russell & Company",
            "Interwest Safety Supply, LLC",
            "Lee Drywall Inc",
            "Little Valley Wholesale Nursery",
            "Midwest Capital Holdings Inc",
            "Professional Home Health Care Inc.",
            "Trans United Inc",
            "Wheaton Group, Inc. dba Visiting Angels",
            "Ziker Cleaners Inc",
            "CDI - Aiken Pest Control Inc",
            "CDI - Allgood Services Inc",
            "CDI - Bug Busters USA",
            "CDI - Capital Tower & Communications, Inc.",
            "CDI - CEMTEK Environmental, Inc.",
            "CDI - Coast Packing Company",
            "CDI - Cycle Construction Company, LLC",
            "CDI - Danville Public Building Commission",
            "CDI - Electronic Source Company",
            "CDI - Environmental Transportation of Nevada",
            "CDI - Essential Cabinetry Group",
            "CDI - F.J. Kerrigan Plumbing Company, Inc.",
            "CDI - Joseph L. Blakan, Inc.",
            "CDI - Lutheran Social Ministries of New Jersey, Inc.",
            "CDI - Malouf Construction, LLC",
            "CDI - McLean County 911 Communications Center",
            "CDI - Merit Contracting, Inc.",
            "CDI - PMI Iowa, LLC",
            "CDI - Rose Pest Solutions - BioServe",
            "CDI - Tri-Dim Filter Corporation",
            "CDI - Ventura Pest Control Inc",
            "CDI - Village of Chatham",
            "CDI - Village of South Holland",
            "CDI (IPMG) - Clinton County Sheriff's Office",
            "CDI (IPMG) - Kendall County Emergency Phone Service and Communication Board",
            "CDI (IPMG) - Kendall County Sheriff's Office",
            "CDI (IPMG) - Oak Park Township",
            "CDI (IPMG) - Village of Forest Park",
            "CDI (IPMG) - Village of Oakwood Hills",
            "CDI (IPMG) - Village of Riverwoods",
            "Apex Transportation Inc",
            "Applied Aerospace Structures Corp.",
            "Aspen Painting, Inc.",
            "AWI Management Corporation",
            "Barr Brands International, Inc.",
            "Bay Area Transportation Authority",
            "BG Products, Incorporated",
            "Bixler Corporation",
            "Brister-Stephens, Inc.",
            "ChemDesign Products, Inc.",
            "Clemson Distribution, Inc.",
            "Daniels Tire Service",
            "Donley Service Center, Inc.",
            "Dove Pointe, Inc.",
            "Heritage Landscape Services, Inc.",
            "Heritage Provider Network, Inc.",
            "Highwoods Contracting Corporation",
            "HNM Systems",
            "International Aerospace Coatings, Inc.",
            "International Risk Management Institute, Inc.",
            "Mega Rentals, Inc.",
            "Montana Lines",
            "New Leaf Landscape Services, Inc.",
            "NOW Health Group, Inc.",
            "Prime Plumbing Incorporated",
            "Social Model Recovery Systems, Inc.",
            "The WaterLeaf at Land Park",
            "Tipton Asset Group, Inc.",
            "Town of Carbondale",
            "Velocity Constructors, Inc.",
            "Village Management Services, Inc.",
            "York Imperial Plastics, Inc.",
            "O'Hara Corporation",
            "Smith Senior Living",
            "Smart Care Equipment Solutions (EEC Acquisition, LLC)",
            "International Marine and Industrial Applicators, I",
            "Carmelite Sisters of the Devine Heart, of Missouri",
            "Wright & McGill Co",
            "Kaluzny Bros. Inc.",
            "Penn Beer Distributors, Inc.",
            "Atlanta Bonded Warehouse Corporation",
            "Behrens and Associates, Inc.",
            "Kenpat USA LLC",
            "Singletrack Electric LLC",
            "D. Armstrong Contracting, LLC",
            "Restoneovation, LLC",
            "Sequoia Beverage, Company, LP",
            "Bill's Tool Rental, Inc.",
            "Midwest Transit Equipment, Inc.",
            "Oak Ridge Winery, LLC",
            "Hawaii Tire Co., LLC",
            "Unity Works Lighting LLC",
            "Matthews Studio Equipment, Inc.",
            "Central Phoenix Eye Care",
            "Rex Heat Treat - Lansdale, Inc.",
            "Frank Motors, Inc.",
            "North American Roofing Services, Inc.",
            "Alan's Lawn and Garden Center, Inc.",
            "Murray & Stafford Inc",
            "F.B. Harding, Inc.",
            "FIRST Services Corp of Orlando",
            "Southwest Aquatics (Desert Limnologists, Inc.)",
            "CDI - Rottler Pest and Lawn Solutions",
            "CDI - Premier Magnesia, LLC",
            "CDI - Baker Rock Resources",
            "CDI - Earth Island",
            "CDI - City of Macomb, Illinois",
            "CDI - Bloomingdale Fire Protection District",
            "CDI - CPI Acquisitions, LLC",
            "CDI - American Paper & Twine Company",
            "CDI - Banks Construction Company",
            "CDI - Pestcom Pest Management LLC",
            "CDI - City of Momence",
            "CDI - Oregon Mainline Paving, LLC",
            "CDI - Palos Fire Protection District",
            "CDI - City of Mattoon Police Department",
            "CDI - Insurance Program Managers Group",
            "CDI (IPMG) - City of Carlyle",
            "CDI (IPMG) - Barrington Police Department",
            "Heath Village",
            "Overeasy, Inc.",
            "Elite Manufacturing & Professional Services, Inc.",
            "Summit Consulting Services, Inc.",
            "The Maids International",
            "Bret's Electric, LLC"
    );

    public static final Map<String, String> MIGRATED_COMPANIES = new HashMap<>() {{
        put("0043", "");
        put("0162", "");
        put("0283", "");
        put("0336", "");
        put("0529", "");
        put("0743", "");
        put("0783", "");
        put("0855", "");
        put("0875", "");
        put("0961", "");
        put("0993", "");
        put("1099", "");
        put("1117", "");
        put("1148", "");
        put("1176", "");
        put("1182", "");
        put("1220", "1");
        put("1240", "");
        put("1242", "");
        put("1256", "");
        put("1292", "");
        put("1297", "");
        put("1310", "");
        put("1318", "");
        put("1338", "");
        put("1356", "");
        put("1387", "");
        put("1466", "");
        put("1498", "");
        put("1515", "");
        put("1530", "");
        put("1541", "");
        put("1557", "");
        put("1581", "");
        put("1592", "");
        put("1608", "");
        put("1662", "");
        put("1678", "");
        put("1714", "");
        put("1735", "");
        put("1770", "");
        put("1792", "");
        put("1795", "");
        put("1815", "");
        put("1823", "");
        put("1874", "");
        put("1889", "");
        put("1896", "");
        put("1915", "");
        put("1936", "");
        put("1937", "");
        put("1941", "");
        put("1942", "");
        put("1950", "");
        put("1966", "");
        put("1970", "");
        put("1973", "");
        put("1984", "");
        put("1985", "");
        put("1990", "");
        put("2010", "");
        put("2015", "");
        put("2016", "");
        put("2022", "1");
        put("2025", "");
        put("2034", "");
        put("2036", "");
        put("2037", "");
        put("2047", "");
        put("2050", "");
        put("2054", "");
        put("2055", "");
        put("2057", "");
        put("2078", "");
        put("2083", "");
        put("2085", "");
        put("2096", "");
        put("2097", "1");
        put("2098", "");
        put("2115", "");
        put("2116", "");
        put("2138", "");
        put("2142", "1");
        put("2146", "");
        put("2148", "");
        put("2149", "");
        put("3370", "");
    }};
}
