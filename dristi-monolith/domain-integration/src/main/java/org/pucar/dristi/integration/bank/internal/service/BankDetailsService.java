package org.pucar.dristi.integration.bank.internal.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.integration.bank.internal.repository.BankDetailsRepository;
import org.pucar.dristi.common.contract.bank.BankDetails;
import org.pucar.dristi.common.contract.bank.BankDetailsSearchCriteria;
import org.pucar.dristi.common.contract.bank.BankDetailsSearchRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BankDetailsService {

    private final BankDetailsRepository bankDetailsRepository;

    public BankDetailsService(BankDetailsRepository bankDetailsRepository) {
        this.bankDetailsRepository = bankDetailsRepository;
    }

    public List<BankDetails> searchBankDetails(BankDetailsSearchRequest request) {

        List<BankDetails> bankDetailsList = new ArrayList<>();

        for(BankDetailsSearchCriteria criteria: request.getCriteria()) {
            String ifsc = criteria.getIfsc();
            JsonNode response = bankDetailsRepository.fetchBankDetails(ifsc);

            if(response == null){
                throw new CustomException("BANK_DETAILS_NOT_FOUND", "Bank details not found for ifsc: " + ifsc);
            }

            String bankName = response.path("BANK").asText(null);
            String branch = response.path("BRANCH").asText(null);
            String ifscCode = response.path("IFSC").asText(null);

            if (bankName == null || branch == null || ifscCode == null) {
                log.info("Incomplete bank details in response for IFSC: {}", ifsc);
            }

            BankDetails bankDetails = BankDetails.builder()
                    .name(bankName)
                    .branch(branch)
                    .ifsc(ifscCode)
                    .build();

            bankDetailsList.add(bankDetails);
        }

        return bankDetailsList;


    }
}
