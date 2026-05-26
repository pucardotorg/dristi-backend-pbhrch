package org.pucar.dristi.integration.summons.internal.repository;

import org.pucar.dristi.integration.summons.internal.repository.querybuilder.SummonsDeliveryQueryBuilder;
import org.pucar.dristi.integration.summons.internal.repository.rowmapper.SummonsDeliveryRowMapper;
import org.pucar.dristi.common.contract.summons.SummonsDelivery;
import org.pucar.dristi.common.contract.summons.SummonsDeliverySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class SummonsRepository {

    private final JdbcTemplate jdbcTemplate;

    private final SummonsDeliveryQueryBuilder queryBuilder;

    private final SummonsDeliveryRowMapper rowMapper;

    public SummonsRepository(JdbcTemplate jdbcTemplate, SummonsDeliveryQueryBuilder queryBuilder, SummonsDeliveryRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryBuilder = queryBuilder;
        this.rowMapper = rowMapper;
    }


    public List<SummonsDelivery> getSummons(SummonsDeliverySearchCriteria searchCriteria) {
        List<String> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSummonsQuery(searchCriteria, preparedStmtList);
        log.debug("Final query: " + query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
    }
}