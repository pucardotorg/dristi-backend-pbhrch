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

    /**
     * Updates transcript / additional-attendees on an existing hearing —
     * mirrors {@code /hearing/v1/update_transcript_additional_attendees}.
     * Predates and is distinct from {@link #updateHearing}.
     */
    void update(HearingRequest request);

    /**
     * Create a new hearing — mirrors {@code /hearing/v1/create}.
     */
    Hearing createHearing(HearingRequest request);

    /**
     * Mainline hearing workflow update — mirrors {@code /hearing/v1/update}.
     * Distinct from {@link #update} (transcript-additional-attendees);
     * callers swapping from REST should match endpoint to method by URI.
     */
    Hearing updateHearing(HearingRequest request);
}
