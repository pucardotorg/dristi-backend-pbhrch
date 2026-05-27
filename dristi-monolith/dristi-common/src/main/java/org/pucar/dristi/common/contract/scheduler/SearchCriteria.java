// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.scheduler;



public interface SearchCriteria {

    String getTenantId();

    String getJudgeId();

    String getCourtId();

    Long getFromDate();

    Long getToDate();
}
