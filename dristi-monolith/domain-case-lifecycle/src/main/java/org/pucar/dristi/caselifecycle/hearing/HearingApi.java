package org.pucar.dristi.caselifecycle.hearing;

import org.pucar.dristi.common.contract.hearing.Hearing;
import org.pucar.dristi.common.contract.hearing.HearingRequest;
import org.pucar.dristi.common.contract.hearing.HearingSearchRequest;

import java.util.List;

/**
 * Public API of the hearing subdomain. Other subdomains (cases today) consume
 * hearing through this interface — never by importing from internal/.
 */
public interface HearingApi {

    List<Hearing> search(HearingSearchRequest request);

    void update(HearingRequest request);
}
