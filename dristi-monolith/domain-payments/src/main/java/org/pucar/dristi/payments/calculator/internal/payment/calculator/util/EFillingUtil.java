package org.pucar.dristi.payments.calculator.internal.payment.calculator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.EFilingParam;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

import static org.pucar.dristi.payments.calculator.internal.payment.calculator.config.ServiceConstants.CASE_MODULE;
import static org.pucar.dristi.payments.calculator.internal.payment.calculator.config.ServiceConstants.E_FILLING_MASTER;

import org.pucar.dristi.common.util.MdmsUtil;
@Component
public class EFillingUtil {

    private final MdmsUtil mdmsUtil;

    private final ObjectMapper objectMapper;

    @Autowired
    public EFillingUtil(MdmsUtil mdmsUtil, ObjectMapper objectMapper) {
        this.mdmsUtil = mdmsUtil;
        this.objectMapper = objectMapper;
    }

    public EFilingParam getEFillingDefaultData(RequestInfo requestInfo, String tenantId) {


        Map<String, Map<String, JSONArray>> response = mdmsUtil.fetchMdmsData(requestInfo, tenantId, CASE_MODULE, Collections.singletonList(E_FILLING_MASTER));
        JSONArray array = response.get(CASE_MODULE).get(E_FILLING_MASTER);
        Object object = array.get(0);

        return objectMapper.convertValue(object, EFilingParam.class);

    }
}
