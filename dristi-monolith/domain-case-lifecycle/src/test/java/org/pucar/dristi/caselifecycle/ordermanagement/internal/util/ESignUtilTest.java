package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.integration.esign.EsignApi;
import org.pucar.dristi.common.contract.ordermanagement.Coordinate;
import org.pucar.dristi.common.contract.ordermanagement.CoordinateRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ESignUtilTest {

    @Mock
    private EsignApi esignApi;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private ESignUtil eSignUtil;

    private CoordinateRequest coordinateRequest;

    @BeforeEach
    void setUp() {
        coordinateRequest = new CoordinateRequest();
    }

    @Test
    void testGetCoordinateForSign_Success() {
        org.pucar.dristi.common.contract.esign.Coordinate apiCoord =
                new org.pucar.dristi.common.contract.esign.Coordinate(
                        10.0F, 20.0F, true, 1, "fileStoreId1", "tenant1");
        when(esignApi.getLocationForSign(any(org.pucar.dristi.common.contract.esign.CoordinateRequest.class)))
                .thenReturn(Collections.singletonList(apiCoord));

        List<Coordinate> result = eSignUtil.getCoordinateForSign(coordinateRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("fileStoreId1", result.get(0).getFileStoreId());
        verify(esignApi, times(1))
                .getLocationForSign(any(org.pucar.dristi.common.contract.esign.CoordinateRequest.class));
    }

    @Test
    void testGetCoordinateForSign_NullResult() {
        when(esignApi.getLocationForSign(any(org.pucar.dristi.common.contract.esign.CoordinateRequest.class)))
                .thenReturn(null);

        List<Coordinate> result = eSignUtil.getCoordinateForSign(coordinateRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCoordinateForSign_Exception() {
        when(esignApi.getLocationForSign(any(org.pucar.dristi.common.contract.esign.CoordinateRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        CustomException exception = assertThrows(CustomException.class,
                () -> eSignUtil.getCoordinateForSign(coordinateRequest));
        assertEquals("Error occurred while getting coordinates", exception.getMessage());
    }
}
