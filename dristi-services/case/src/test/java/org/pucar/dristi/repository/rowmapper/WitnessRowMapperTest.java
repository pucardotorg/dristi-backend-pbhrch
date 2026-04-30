package org.pucar.dristi.repository.rowmapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pucar.dristi.web.models.Witness;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class WitnessRowMapperTest {

    @InjectMocks
    private WitnessRowMapper witnessRowMapper;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() {
        witnessRowMapper = new WitnessRowMapper(null);
        resultSet = mock(ResultSet.class);
    }

    @Test
    public void testExtractData_Success() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
        when(resultSet.getString("caseid")).thenReturn("case123");
        when(resultSet.getString("filingnumber")).thenReturn("file123");
        when(resultSet.getString("cnrnumber")).thenReturn("cnr123");
        when(resultSet.getString("individualid")).thenReturn("individual123");
        when(resultSet.getString("witnessidentifier")).thenReturn("witness123");
        when(resultSet.getString("remarks")).thenReturn("remark123");
        when(resultSet.getBoolean("isactive")).thenReturn(true);
        when(resultSet.getString("createdby")).thenReturn("user123");
        Timestamp createdTimestamp = Timestamp.from(OffsetDateTime.now(ZoneId.of("UTC")).toInstant());
        when(resultSet.getTimestamp("createdtime")).thenReturn(createdTimestamp);
        when(resultSet.getString("lastmodifiedby")).thenReturn("user456");
        Timestamp lastModifiedTimestamp = Timestamp.from(OffsetDateTime.now(ZoneId.of("UTC")).toInstant());
        when(resultSet.getTimestamp("lastmodifiedtime")).thenReturn(lastModifiedTimestamp);

        // Mocking additionalDetails as PGObject
        ObjectMapper objectMapper = new ObjectMapper();
        String additionalDetailsJson = "{\"key\": \"value\"}";
        when(resultSet.getObject("additionalDetails")).thenReturn(null); // Mocking as null for simplicity

        // Act
        List<Witness> witnesses = witnessRowMapper.extractData(resultSet);

        // Assert
        assertEquals(1, witnesses.size());
        Witness witness = witnesses.get(0);
        assertEquals("case123", witness.getCaseId());
        assertEquals("file123", witness.getFilingNumber());
        assertEquals("cnr123", witness.getCnrNumber());
        assertEquals("individual123", witness.getIndividualId());
        assertEquals("witness123", witness.getWitnessIdentifier());
        assertEquals("remark123", witness.getRemarks());
        assertEquals(true, witness.getIsActive());
        assertEquals("user123", witness.getAuditDetails().getCreatedBy());
        assertEquals("user456", witness.getAuditDetails().getLastModifiedBy());
        // Test that additionalDetails is not set (as we mocked it to null)
        assertEquals(null, witness.getAdditionalDetails());
    }

    @Test
    void testExtractData_Exception() throws Exception {
        when(resultSet.next()).thenThrow(new SQLException("Database error"));

        assertThrows(Exception.class, () -> witnessRowMapper.extractData(resultSet));
    }

    @Test
    void testExtractData_CustomException() throws Exception {
        when(resultSet.next()).thenThrow(new CustomException());

        assertThrows(CustomException.class, () -> witnessRowMapper.extractData(resultSet));
    }
}
