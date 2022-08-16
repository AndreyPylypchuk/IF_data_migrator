package com.tht.ifdatamigrator.service.restore;

import static java.lang.String.format;

public final class RestoreUtils {

    public static String generateJobpostTitle(String version) {
        return format("IntegrityFirst (%s)", version);
    }
}
