package org.pucar.dristi.integration.njdg.internal.repository.rowmapper;

import org.pucar.dristi.integration.njdg.internal.model.ExtraAdvocateDetails;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdvocateRowMapper implements RowMapper<ExtraAdvocateDetails> {

    @Override
    public ExtraAdvocateDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ExtraAdvocateDetails.builder()
                .id(rs.getInt("id"))
                .partyNo(rs.getInt("party_no"))
                .cino(rs.getString("cino"))
                .petResName(rs.getString("pet_res_name"))
                .type(rs.getInt("type"))
                .advName(rs.getString("adv_name"))
                .advCode(rs.getInt("adv_code"))
                .srNo(rs.getInt("sr_no"))
                .build();
    }
}
