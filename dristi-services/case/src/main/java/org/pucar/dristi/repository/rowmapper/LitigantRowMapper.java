package org.pucar.dristi.repository.rowmapper;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.pucar.dristi.web.models.Address;
import org.pucar.dristi.web.models.ComplainantTypeInfo;
import org.pucar.dristi.web.models.ComplainantTypeOfEntity;
import org.pucar.dristi.web.models.Party;
import org.pucar.dristi.web.models.TransferredPOAInfo;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LitigantRowMapper implements ResultSetExtractor<Map<UUID, List<Party>>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<UUID, List<Party>> extractData(ResultSet rs) {
        Map<UUID, List<Party>> partyMap = new LinkedHashMap<>();

        try {
            while (rs.next()) {
                String id = rs.getString("case_id");
                UUID uuid = UUID.fromString(id != null ? id : "00000000-0000-0000-0000-000000000000");

                Long lastModifiedTime = rs.getLong("lastmodifiedtime");

                AuditDetails auditdetails = AuditDetails.builder()
                        .createdBy(rs.getString("createdby"))
                        .createdTime(rs.getLong("createdtime"))
                        .lastModifiedBy(rs.getString("lastmodifiedby"))
                        .lastModifiedTime(lastModifiedTime)
                        .build();
                Party party = Party.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .tenantId(rs.getString("tenantid"))
                        .partyCategory(rs.getString("partycategory"))
                        .individualId(rs.getString("individualid"))
                        .organisationID(rs.getString("organisationid"))
                        .partyType(rs.getString("partytype"))
                        .isActive(rs.getBoolean("isactive"))
                        .caseId(rs.getString("case_id"))
                        .hasSigned(rs.getBoolean("hassigned"))
                        .isResponseRequired(rs.getBoolean("isresponserequired"))
                        .firstName(rs.getString("first_name"))
                        .middleName(rs.getString("middle_name"))
                        .lastName(rs.getString("last_name"))
                        .fullName(rs.getString("full_name"))
                        .mobileNumber(rs.getString("mobile_number"))
                        .age(rs.getString("age"))
                        .companyName(rs.getString("company_name"))
                        .designation(rs.getString("designation"))
                        .complainantType(getObjectFromJson(rs.getString("complainant_type"), ComplainantTypeInfo.class))
                        .complainantTypeOfEntity(getObjectFromJson(rs.getString("complainant_type_of_entity"), ComplainantTypeOfEntity.class))
                        .transferredPOA(getObjectFromJson(rs.getString("transferred_poa"), TransferredPOAInfo.class))
                        .permanentAddress(getObjectFromJson(rs.getString("permanent_address"), Address.class))
                        .currentAddress(getObjectFromJson(rs.getString("current_address"), Address.class))
                        .companyAddress(getObjectFromJson(rs.getString("company_address"), Address.class))
                        .auditDetails(auditdetails)
                        .build();


                PGobject pgObject = (PGobject) rs.getObject("additionalDetails");
                if (pgObject != null)
                    party.setAdditionalDetails(objectMapper.readTree(pgObject.getValue()));

                if (partyMap.containsKey(uuid)) {
                    partyMap.get(uuid).add(party);
                } else {
                    List<Party> parties = new ArrayList<>();
                    parties.add(party);
                    partyMap.put(uuid, parties);
                }
            }
        } catch(CustomException e){
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while processing Case ResultSet :: {}", e.toString());
            throw new CustomException("ROW_MAPPER_EXCEPTION", "Exception occurred while processing Case ResultSet: " + e.getMessage());
        }
        return partyMap;
    }

    private <T> T getObjectFromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new CustomException("ROW_MAPPER_EXCEPTION", "Failed to convert JSON to " + clazz.getSimpleName() + ": " + e.getMessage());
        }
    }
}
