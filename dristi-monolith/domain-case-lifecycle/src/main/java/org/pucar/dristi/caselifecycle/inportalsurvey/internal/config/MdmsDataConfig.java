package org.pucar.dristi.caselifecycle.inportalsurvey.internal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.util.MdmsUtil;
import org.pucar.dristi.caselifecycle.inportalsurvey.internal.web.models.SurveyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("inportalsurveyMdmsDataConfig")
@Slf4j
public class MdmsDataConfig {

    private final MdmsUtil mdmsUtil;
    private final Configuration configuration;
    private final ObjectMapper objectMapper;

    @Autowired
    public MdmsDataConfig(MdmsUtil mdmsUtil, Configuration configuration, ObjectMapper objectMapper) {
        this.mdmsUtil = mdmsUtil;
        this.configuration = configuration;
        this.objectMapper = objectMapper;
    }

    public SurveyConfig fetchSurveyConfig(RequestInfo requestInfo) {
        try {
            String tenantId = requestInfo.getUserInfo().getTenantId();
            log.info("Fetching survey configuration from MDMS for tenantId: {}", tenantId);
            
            JSONArray surveyConfigList = mdmsUtil.fetchMdmsData(
                    requestInfo,
                    tenantId,
                    configuration.getMdmsSurveyModuleName(),
                    List.of(configuration.getMdmsSurveyMasterName())
            ).get(configuration.getMdmsSurveyModuleName()).get(configuration.getMdmsSurveyMasterName());

            if (surveyConfigList != null && !surveyConfigList.isEmpty()) {
                SurveyConfig surveyConfig = objectMapper.convertValue(surveyConfigList.get(0), SurveyConfig.class);
                log.info("Survey configuration fetched from MDMS: {}", surveyConfig);
                return surveyConfig;
            } else {
                log.warn("No survey configuration found in MDMS, using default values");
                return getDefaultSurveyConfig();
            }
        } catch (Exception e) {
            log.error("Unable to fetch survey configuration from MDMS, using default values. Error: {}", e.getMessage());
            return getDefaultSurveyConfig();
        }
    }

    private SurveyConfig getDefaultSurveyConfig() {
        return SurveyConfig.builder()
                .noOfDaysForRemindMeLater(172800000L)
                .maxNoOfAttempts(3)
                .noOfDaysForExpiryAfterFeedBack(5184000000L)
                .build();
    }
}
