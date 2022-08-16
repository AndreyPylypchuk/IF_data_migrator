package com.tht.ifdatamigrator.service.backup;

public final class BackupUtils {

    public static Integer mapQuality(String category) {
        if (category.endsWith("THEFT")) return 1338;
        if (category.endsWith("FAKES")) return 1339;
        if (category.endsWith("DRUGS")) return 1340;
        if (category.endsWith("HOSTL")) return 1341;
        if (category.endsWith("FILLR")) return 1342;
        if (category.endsWith("SAFETY")) return 1344;
        if (category.endsWith("HEALTH")) return 1345;
        if (category.endsWith("INDUSTRY")) return 1346;
        if (category.endsWith("SAFTEY")) return 1344;
        return null;
    }
}
