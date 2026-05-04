package org.pucar.dristi.caselifecycle.order.internal.web.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OrderPaginationTest {

    @Test
    public void testToString() {
        Assertions.assertEquals("asc", OrderPagination.ASC.toString());
        Assertions.assertEquals("desc", OrderPagination.DESC.toString());
    }

    @Test
    public void testFromValue() {
        Assertions.assertEquals(OrderPagination.ASC, OrderPagination.fromValue("asc"));
        Assertions.assertEquals(OrderPagination.DESC, OrderPagination.fromValue("desc"));
        Assertions.assertNull(OrderPagination.fromValue("invalid"));
    }
}