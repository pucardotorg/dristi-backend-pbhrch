package org.pucar.dristi.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pucar.dristi.web.models.Order;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

class OrderRowMapperTest {

    private OrderRowMapper orderRowMapper;

    private ResultSet resultSet;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        orderRowMapper = new OrderRowMapper(objectMapper, null);
        resultSet = mock(ResultSet.class);
    }

    @Test
    void testExtractData() throws Exception {
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("id")).thenReturn("123e4567-e89b-12d3-a456-556642440000");
        when(resultSet.getString("tenantid")).thenReturn("tenant-123");
        when(resultSet.getString("ordernumber")).thenReturn("ORDER-123");
        when(resultSet.getString("linkedordernumber")).thenReturn("LINKED-ORDER-123");
        when(resultSet.getString("hearingnumber")).thenReturn("123e4567-e89b-12d3-a456-556642440001");
        when(resultSet.getString("hearingtype")).thenReturn("Type1");
        when(resultSet.getString("cnrnumber")).thenReturn("CNR-123");
        when(resultSet.getString("ordercategory")).thenReturn("Category1");
        when(resultSet.getBoolean("isactive")).thenReturn(true);
        when(resultSet.getString("ordertype")).thenReturn("Type1");
        when(resultSet.getTimestamp("createddate")).thenReturn(null);
        when(resultSet.getString("comments")).thenReturn("Test comment");
        when(resultSet.getString("filingnumber")).thenReturn("FILING-123");
        when(resultSet.getString("ordertitle")).thenReturn("Test Order Title");
        when(resultSet.getString("status")).thenReturn("Active");
        when(resultSet.getString("createdby")).thenReturn("user-123");
        when(resultSet.getTimestamp("createdtime")).thenReturn(null);
        when(resultSet.getString("lastmodifiedby")).thenReturn("user-123");
        when(resultSet.getTimestamp("lastmodifiedtime")).thenReturn(null);
        when(resultSet.wasNull()).thenReturn(false);

        // Mock IssuedBy JSON with correct structure
        String issuedByJson = "{\"benchID\":\"bench-123\",\"judgeID\":[\"123e4567-e89b-12d3-a456-556642440002\",\"123e4567-e89b-12d3-a456-556642440003\"],\"courtID\":\"court-123\"}";
        when(resultSet.getString("issuedby")).thenReturn(issuedByJson);

        // Mock compositeItems JSON with the correct structure
        String compositeItemsJson = "[{\"id\":\"item-1\",\"orderType\":\"Type1\",\"orderSchema\":{\"schema\":\"value\"}},{\"id\":\"item-2\",\"orderType\":\"Type2\",\"orderSchema\":{\"schema\":\"value2\"}}]";
        when(resultSet.getString("compositeitems")).thenReturn(compositeItemsJson);

        // Mock additionaldetails PGobject
        PGobject pgObject = new PGobject();
        pgObject.setType("json");
        pgObject.setValue("{\"key\":\"value\"}");
        when(resultSet.getObject("additionaldetails")).thenReturn(pgObject);

        // Mock applicationnumber PGobject
        PGobject pgObjectApp = new PGobject();
        pgObjectApp.setType("json");
        pgObjectApp.setValue("[\"APP-1\",\"APP-2\"]");
        when(resultSet.getObject("applicationnumber")).thenReturn(pgObjectApp);

        // Mock orderDetails PGobject
        PGobject pgObjectDetails = new PGobject();
        pgObjectDetails.setType("json");
        pgObjectDetails.setValue("{\"details\":\"value\"}");
        when(resultSet.getObject("orderDetails")).thenReturn(pgObjectDetails);

        List<Order> orders = orderRowMapper.extractData(resultSet);

        assertNotNull(orders);
        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-556642440000"), order.getId());
        assertEquals("tenant-123", order.getTenantId());
        assertEquals("ORDER-123", order.getOrderNumber());
        assertEquals("LINKED-ORDER-123", order.getLinkedOrderNumber());
        assertEquals("123e4567-e89b-12d3-a456-556642440001", order.getHearingNumber());
        assertEquals("Type1", order.getHearingType());
        assertEquals("CNR-123", order.getCnrNumber());
        assertEquals("Category1", order.getOrderCategory());
        assertTrue(order.getIsActive());
        assertEquals("Type1", order.getOrderType());
        assertNull(order.getCreatedDate());
        assertEquals("Test comment", order.getComments());
        assertEquals("FILING-123", order.getFilingNumber());
        assertEquals("Test Order Title", order.getOrderTitle());
        assertEquals("Active", order.getStatus());

        // Verify IssuedBy with correct structure
        assertNotNull(order.getIssuedBy());
        assertEquals("bench-123", order.getIssuedBy().getBenchID());
        assertEquals("court-123", order.getIssuedBy().getCourtID());
        assertNotNull(order.getIssuedBy().getJudgeID());
        assertEquals(2, order.getIssuedBy().getJudgeID().size());
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-556642440002"), order.getIssuedBy().getJudgeID().get(0));
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-556642440003"), order.getIssuedBy().getJudgeID().get(1));

        // Verify compositeItems
        assertNotNull(order.getCompositeItems());

        // Verify AuditDetails
        assertNotNull(order.getAuditDetails());
        assertEquals("user-123", order.getAuditDetails().getCreatedBy());
        assertNull(order.getAuditDetails().getCreatedTime());
        assertEquals("user-123", order.getAuditDetails().getLastModifiedBy());
        assertNull(order.getAuditDetails().getLastModifiedTime());

        // Verify the PGObject fields
        assertNotNull(order.getAdditionalDetails());
        assertNotNull(order.getApplicationNumber());
        assertEquals(2, order.getApplicationNumber().size());
        assertNotNull(order.getOrderDetails());
    }

    @Test
    void testExtractDataWithException() throws Exception {
        when(resultSet.next()).thenThrow(new SQLException("Test exception"));

        CustomException thrown = assertThrows(CustomException.class, () -> {
            orderRowMapper.extractData(resultSet);
        });

        assertEquals("ROW_MAPPER_EXCEPTION", thrown.getCode());
        assertTrue(thrown.getMessage().contains("Test exception"));
    }
}