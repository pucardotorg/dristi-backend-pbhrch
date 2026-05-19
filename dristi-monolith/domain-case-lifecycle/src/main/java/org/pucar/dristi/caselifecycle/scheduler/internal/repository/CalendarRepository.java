package org.pucar.dristi.caselifecycle.scheduler.internal.repository;


import org.pucar.dristi.caselifecycle.scheduler.internal.repository.querybuilder.CalendarQueryBuilder;
import org.pucar.dristi.caselifecycle.scheduler.internal.repository.rowmapper.CalendarRowMapper;
import org.pucar.dristi.common.contract.scheduler.JudgeCalendarRule;
import org.pucar.dristi.common.contract.scheduler.JudgeCalenderSearchRequest;
import org.pucar.dristi.common.contract.scheduler.SearchCriteria;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class CalendarRepository {

    private final CalendarQueryBuilder queryBuilder;
    private final CalendarRowMapper rowMapper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CalendarRepository(CalendarQueryBuilder queryBuilder, CalendarRowMapper rowMapper, JdbcTemplate jdbcTemplate) {
        this.queryBuilder = queryBuilder;
        this.rowMapper = rowMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<JudgeCalendarRule> getJudgeRule(SearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getJudgeCalendarQuery(criteria, preparedStmtList);
        log.debug("Final query: " + query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
    }

    public List<JudgeCalendarRule> findJudgeRule(JudgeCalenderSearchRequest request) {
        List<Object> preparedStmtList = new ArrayList<>();
        List<Integer> preparedStmtArgList = new ArrayList<>();
        String query = queryBuilder.findJudgeCalendarQuery(request, preparedStmtList, preparedStmtArgList);
        log.debug("query: " + query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), preparedStmtArgList.stream().mapToInt(Integer::intValue).toArray(), rowMapper);
    }
}
