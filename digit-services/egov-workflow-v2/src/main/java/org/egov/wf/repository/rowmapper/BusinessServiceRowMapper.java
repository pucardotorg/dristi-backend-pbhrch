package org.egov.wf.repository.rowmapper;

import org.egov.wf.web.models.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
public class BusinessServiceRowMapper implements ResultSetExtractor<List<BusinessService>> {

    public List<BusinessService> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String,BusinessService> businessServiceMap = new HashMap<>();

        while (rs.next()){
            String uuid = rs.getString("bs_uuid");
            BusinessService businessService = businessServiceMap.get(uuid);
            if(businessService==null){
                java.sql.Timestamp bsLastModTs = rs.getTimestamp("bs_lastModifiedTime");
                java.time.OffsetDateTime bsLastModifiedTime = bsLastModTs != null ? bsLastModTs.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;
                java.sql.Timestamp bsCreatedTs = rs.getTimestamp("bs_createdTime");
                java.time.OffsetDateTime bsCreatedTime = bsCreatedTs != null ? bsCreatedTs.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;
                AuditDetails auditdetails = AuditDetails.builder()
                        .createdBy(rs.getString("bs_createdBy"))
                        .createdTime(bsCreatedTime)
                        .lastModifiedBy(rs.getString("bs_lastModifiedBy"))
                        .lastModifiedTime(bsLastModifiedTime)
                        .build();
                businessService = BusinessService.builder()
                        .tenantId(rs.getString("bs_tenantId"))
                        .getUri(rs.getString("geturi"))
                        .postUri(rs.getString("posturi"))
                        .businessService(rs.getString("businessService"))
                        .business(rs.getString("business"))
                        .uuid(uuid)
                        .businessServiceSla(rs.getLong("businessservicesla"))
                        .auditDetails(auditdetails)
                        .build();
                businessServiceMap.put(uuid,businessService);
            }
            addChildrenToBusinessService(rs,businessService);
        }
        return new LinkedList<>(businessServiceMap.values());
    }




    /**
     *  Adds child object's (States and Actions) to parent Object(BusinessService)
     * @param rs The result set from sql query
     * @param businessService The parent object
     * @throws SQLException
     */
    private void addChildrenToBusinessService(ResultSet rs,BusinessService businessService) throws SQLException{

        String stateUuid = rs.getString("st_uuid");
        String actionUuid = rs.getString("ac_uuid");

        java.sql.Timestamp stLastModTs = rs.getTimestamp("st_lastModifiedTime");
        java.time.OffsetDateTime lastModifiedTime = stLastModTs != null ? stLastModTs.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;

        State state;
        if(businessService.getStateFromUuid(stateUuid)==null){
            java.sql.Timestamp stCreatedTs = rs.getTimestamp("st_createdTime");
            java.time.OffsetDateTime stCreatedTime = stCreatedTs != null ? stCreatedTs.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;
            AuditDetails auditdetails = AuditDetails.builder()
                    .createdBy(rs.getString("st_createdBy"))
                    .createdTime(stCreatedTime)
                    .lastModifiedBy(rs.getString("st_lastModifiedBy"))
                    .lastModifiedTime(lastModifiedTime)
                    .build();

            Long sla = rs.getLong("sla");
            if (rs.wasNull()) {
                sla = null;
            }

            state = State.builder()
                .tenantId(rs.getString("st_tenantId"))
                .uuid(stateUuid)
                .state(rs.getString("state"))
                .sla(sla)
                .applicationStatus(rs.getString("applicationStatus"))
                .isStartState(rs.getBoolean("isStartState"))
                .isTerminateState(rs.getBoolean("isTerminateState"))
                .docUploadRequired(rs.getBoolean("docuploadrequired"))
                .isStateUpdatable(rs.getBoolean("isStateUpdatable"))
                .businessServiceId(rs.getString("businessserviceid"))
                .auditDetails(auditdetails)
                .build();

            businessService.addStatesItem(state);
        }
        else {
            state = businessService.getStateFromUuid(stateUuid);
        }

        if(actionUuid!=null){
            java.sql.Timestamp acLastModTs = rs.getTimestamp("ac_lastModifiedTime");
            java.time.OffsetDateTime actionLastModifiedTime = acLastModTs != null ? acLastModTs.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;
            java.sql.Timestamp acCreatedTs = rs.getTimestamp("ac_createdTime");
            java.time.OffsetDateTime acCreatedTime = acCreatedTs != null ? acCreatedTs.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;

            AuditDetails actionAuditdetails = AuditDetails.builder()
                    .createdBy(rs.getString("ac_createdBy"))
                    .createdTime(acCreatedTime)
                    .lastModifiedBy(rs.getString("ac_lastModifiedBy"))
                    .lastModifiedTime(actionLastModifiedTime)
                    .build();

            Action action = Action.builder()
                    .tenantId(rs.getString("ac_tenantId"))
                    .action(rs.getString("action"))
                    .nextState(rs.getString("nextState"))
                    .uuid(actionUuid)
                    .currentState(rs.getString("currentState"))
                    .roles(Arrays.asList(rs.getString("roles").split(",")))
                    .active(rs.getBoolean("ac_active"))
                    .auditDetails(actionAuditdetails)
                    .build();
            state.addActionsItem(action);
        }
    }






    }
