package org.pucar.dristi.integration.treasury;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.treasury.Calculation;
import org.pucar.dristi.common.contract.treasury.DemandCreateRequest;
import org.pucar.dristi.common.models.Document;

/**
 * Public, cross-subdomain API of the treasury subdomain. Other modules
 * (cases today) consume treasury through this interface — never by importing
 * from {@code internal/}.
 */
public interface TreasuryApi {

    /**
     * Creates a billing-service demand for the given consumer code and publishes
     * the corresponding treasury head-mapping to Kafka. Side effects span
     * billing-svc (external REST) and the treasury-mapping topic.
     *
     * @param requestInfo eGov request envelope carrying caller identity
     * @param request     demand payload — consumer code, filing number,
     *                    calculation breakdown, entity type
     */
    void createDemand(RequestInfo requestInfo, DemandCreateRequest request);

    /**
     * Returns the persisted payment receipt {@link Document} for a settled bill.
     *
     * @param requestInfo eGov request envelope carrying caller identity
     * @param billId      billing-service bill identifier
     */
    Document getPaymentReceipt(RequestInfo requestInfo, String billId);

    /**
     * Returns the head-breakup {@link Calculation} for a consumer code. When
     * the underlying treasury mapping carries a {@code finalCalcPostResubmission},
     * that is returned; otherwise the original {@code calculation} is returned.
     *
     * @param requestInfo  eGov request envelope carrying caller identity
     * @param consumerCode billing-service consumer code (e.g. filing number + suffix)
     * @return the resolved calculation, or {@code null} if no mapping exists
     */
    Calculation getHeadBreakDownCalculation(RequestInfo requestInfo, String consumerCode);
}
