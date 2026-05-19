package org.pucar.dristi.caselifecycle.scheduler.internal.validator;

import org.pucar.dristi.common.contract.scheduler.JudgeCalendarRule;
import org.pucar.dristi.common.contract.scheduler.SearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JudgeCalendarValidator {


    public void validateUpdateJudgeCalendar(List<JudgeCalendarRule> judgeCalendarRule) {
    }

    public <T extends SearchCriteria> void validateSearchRequest(T criteria) {
        if (criteria.getTenantId() == null)
            throw new CustomException("DK_SH_SEARCH_ERR", "tenantId is mandatory for search");

        if (criteria.getJudgeId() == null)
            throw new CustomException("DK_SH_SEARCH_ERR", "judgeId is mandatory for search");

        if (criteria.getCourtId() == null)
            throw new CustomException("DK_SH_SEARCH_ERR", "courtId is mandatory for search");
    }
}
