package org.pucar.dristi.caselifecycle.scheduler.internal.repository.querybuilder;


import org.pucar.dristi.caselifecycle.scheduler.internal.helper.QueryBuilderHelper;
import org.pucar.dristi.common.contract.scheduler.OptOutSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RescheduleRequestOptOutQueryBuilder {

    private static final String BASE_APPLICATION_QUERY = "SELECT  oo.id ,oo.individual_id ,oo.judge_id ,oo.case_id ,oo.reschedule_request_id ,oo.opt_out_dates , oo.created_by,oo.last_modified_by,oo.created_time,oo.last_modified_time, oo.row_version , oo.tenant_id ";
    private static final String FROM_TABLES = " FROM reschedule_request_opt_out_detail oo ";
    private static final String LIMIT_OFFSET = " LIMIT ? OFFSET ?";

    private final QueryBuilderHelper queryBuilderHelper;

    @Autowired
    public RescheduleRequestOptOutQueryBuilder(QueryBuilderHelper queryBuilderHelper) {
        this.queryBuilderHelper = queryBuilderHelper;
    }

    public String getOptOutQuery(OptOutSearchCriteria optOutSearchCriteria, List<Object> preparedStmtList, Integer limit, Integer offset) {
        StringBuilder query = new StringBuilder(BASE_APPLICATION_QUERY);
        query.append(FROM_TABLES);


        if (optOutSearchCriteria.getJudgeId() != null) {
            queryBuilderHelper.addClauseIfRequired(query, preparedStmtList);
            query.append(" oo.judge_id = ? ");
            preparedStmtList.add(optOutSearchCriteria.getJudgeId());

        }
        if (optOutSearchCriteria.getCaseId() != null) {
            queryBuilderHelper.addClauseIfRequired(query, preparedStmtList);
            query.append(" oo.case_id = ? ");
            preparedStmtList.add(optOutSearchCriteria.getCaseId());

        }

        if (optOutSearchCriteria.getIndividualId() != null) {
            queryBuilderHelper.addClauseIfRequired(query, preparedStmtList);
            query.append(" oo.individual_id = ? ");
            preparedStmtList.add(optOutSearchCriteria.getIndividualId());

        }
        if (optOutSearchCriteria.getRescheduleRequestId() != null) {
            queryBuilderHelper.addClauseIfRequired(query, preparedStmtList);
            query.append(" oo.reschedule_request_id = ? ");
            preparedStmtList.add(optOutSearchCriteria.getRescheduleRequestId());

        }
        if (optOutSearchCriteria.getTenantId() != null) {
            queryBuilderHelper.addClauseIfRequired(query, preparedStmtList);
            query.append(" oo.tenant_id = ? ");
            preparedStmtList.add(optOutSearchCriteria.getTenantId());

        }

        if (limit != null && offset != null) {
            query.append(LIMIT_OFFSET);
            preparedStmtList.add(limit);
            preparedStmtList.add(offset);

        }
        return query.toString();


    }
}
