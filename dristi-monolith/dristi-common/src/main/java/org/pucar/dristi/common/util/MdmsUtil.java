// HAND-CURATED — imports rewired to dristi-common; do not regenerate
// AUTO-EXTRACTED INTO dristi-common BY scripts/migration/dristi_common/03_build_canonical.py
// Source: dristi-services/ab-diary/src/main/java/digit/util/MdmsUtil.java
// NOTE: imports referencing service-internal classes (ServiceConstants,
// CommonConfiguration, web.models.*) may need follow-up — see Phase 4.
package org.pucar.dristi.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pucar.dristi.common.config.CommonConfiguration;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.pucar.dristi.common.config.CommonConstants.*;

@Slf4j
@Component("commonMdmsUtil")
public class MdmsUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CommonConfiguration configs;




    public Map<String, Map<String, JSONArray>> fetchMdmsData(RequestInfo requestInfo, String tenantId, String moduleName,
                                                                                List<String> masterNameList) {
        StringBuilder uri = new StringBuilder();
        uri.append(configs.getMdmsHost()).append(configs.getMdmsEndPoint());
        MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequest(requestInfo, tenantId, moduleName, masterNameList);
        Object response = new HashMap<>();
        Integer rate = 0;
        MdmsResponse mdmsResponse = new MdmsResponse();
        try {
            response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
            mdmsResponse = mapper.convertValue(response, MdmsResponse.class);
        }catch(Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_MDMS,e);
        }

        return mdmsResponse.getMdmsRes();
        //log.info(ulbToCategoryListMap.toString());
    }

    private MdmsCriteriaReq getMdmsRequest(RequestInfo requestInfo, String tenantId,
                                           String moduleName, List<String> masterNameList) {
        List<MasterDetail> masterDetailList = new ArrayList<>();
        for(String masterName: masterNameList) {
            MasterDetail masterDetail = new MasterDetail();
            masterDetail.setName(masterName);
            masterDetailList.add(masterDetail);
        }

        ModuleDetail moduleDetail = new ModuleDetail();
        moduleDetail.setMasterDetails(masterDetailList);
        moduleDetail.setModuleName(moduleName);
        List<ModuleDetail> moduleDetailList = new ArrayList<>();
        moduleDetailList.add(moduleDetail);

        MdmsCriteria mdmsCriteria = new MdmsCriteria();
        mdmsCriteria.setTenantId(tenantId.split("\\.")[0]);
        mdmsCriteria.setModuleDetails(moduleDetailList);

        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
        mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
        mdmsCriteriaReq.setRequestInfo(requestInfo);

        return mdmsCriteriaReq;
    }
}