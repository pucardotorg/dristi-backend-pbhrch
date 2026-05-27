package org.pucar.dristi.caselifecycle.hearingmanagement.internal.config;

import org.springframework.stereotype.Component;

@Component("hearingmanagementServiceConstants")
public class ServiceConstants {

    public static final String DATE_FORMAT_Y_M_D = "yyyy-MM-dd";
    public static final String DATE_FORMAT_D_M_Y = "dd-MM-yyyy";

    public static final String HEARING_SEARCH_EXCEPTION = "HEARING_SEARCH_EXCEPTION";
    public static final String ENRICHMENT_ERROR = "ENRICHMENT_ERROR";

}
