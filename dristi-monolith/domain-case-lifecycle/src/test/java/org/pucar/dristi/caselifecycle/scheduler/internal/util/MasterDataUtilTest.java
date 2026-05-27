package  org.pucar.dristi.caselifecycle.scheduler.internal.util;

import org.pucar.dristi.caselifecycle.scheduler.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.scheduler.internal.config.ServiceConstants;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.pucar.dristi.common.util.MdmsUtil;
@ExtendWith(MockitoExtension.class)
public class MasterDataUtilTest {

    @InjectMocks
    private MasterDataUtil masterDataUtil;

    @Mock
    private MdmsUtil mdmsUtil;

    @Mock
    private ServiceConstants serviceConstants;

    @Mock
    private Configuration config;

    @Test
    void testGetDataFromMDMS() {
        // Mock MDMS response
        Map<String, Map<String, JSONArray>> mockResponse = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(Collections.singletonMap("key", "value"));
        Map<String, JSONArray> courtData = new HashMap<>();
        courtData.put("testMaster", jsonArray);
        mockResponse.put("testModule", courtData);

        when(config.getEgovStateTenantId()).thenReturn("tenantId");
        when(mdmsUtil.fetchMdmsData(any(RequestInfo.class), anyString(), anyString(), anyList()))
                .thenReturn(mockResponse);

        // Call the method
        List<Map> result = masterDataUtil.getDataFromMDMS(Map.class, "testMaster", "testModule");

        // Verify the result
        assertEquals(1, result.size());
        assertEquals("value", result.get(0).get("key"));
    }

    @Test
    void testGetDataFromMDMSWithEmptyResponse() {
        // Mock MDMS response
        Map<String, Map<String, JSONArray>> mockResponse = new HashMap<>();
        Map<String, JSONArray> courtData = new HashMap<>();
        courtData.put("testMaster", new JSONArray());
        mockResponse.put("testModule", courtData);

        when(config.getEgovStateTenantId()).thenReturn("tenantId");
        when(mdmsUtil.fetchMdmsData(any(RequestInfo.class), anyString(), anyString(), anyList()))
                .thenReturn(mockResponse);

        // Call the method
        List<Map> result = masterDataUtil.getDataFromMDMS(Map.class, "testMaster", "testModule");

        // Verify the result
        assertEquals(0, result.size());
    }
}