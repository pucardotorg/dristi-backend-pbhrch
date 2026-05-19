package org.pucar.dristi.caselifecycle.scheduler.internal.web.models;



public interface SearchCriteria {

    String getTenantId();

    String getJudgeId();

    String getCourtId();

    Long getFromDate();

    Long getToDate();
}
