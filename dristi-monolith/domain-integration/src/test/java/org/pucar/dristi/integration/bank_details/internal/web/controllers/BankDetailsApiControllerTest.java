package org.pucar.dristi.integration.bank_details.internal.web.controllers;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.pucar.dristi.integration.bank_details.internal.TestConfiguration;
import org.pucar.dristi.integration.bank_details.internal.service.BankDetailsService;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.integration.bank_details.internal.web.models.BankDetails;
import org.pucar.dristi.integration.bank_details.internal.web.models.BankDetailsSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
* API tests for BankDetailsApiController
*/
@WebMvcTest(BankDetailsApiController.class)
@Import(TestConfiguration.class)
class BankDetailsApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankDetailsService bankDetailsService;

    @MockBean
    private ResponseInfoFactory responseInfoFactory;

    @Test
    void bankDetailsV1SearchPostSuccess() throws Exception {
        when(bankDetailsService.searchBankDetails(any(BankDetailsSearchRequest.class)))
                .thenReturn(Collections.singletonList(BankDetails.builder().ifsc("SBIN0005094").name("Bank").branch("Branch").build()));
        when(responseInfoFactory.createResponseInfoFromRequestInfo(any(RequestInfo.class), Mockito.eq(true)))
                .thenReturn(ResponseInfo.builder().apiId("api").build());

        String body = "{\"RequestInfo\":{},\"criteria\":[{\"ifsc\":\"SBIN0005094\"}]}";

        mockMvc.perform(post("/bank-details/v1/_search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankDetails", hasSize(1)))
                .andExpect(jsonPath("$.bankDetails[0].ifsc").value("SBIN0005094"));
    }

    @Test
    void bankDetailsV1SearchPostBadRequestWhenMissingBody() throws Exception {
        mockMvc.perform(post("/bank-details/v1/_search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
