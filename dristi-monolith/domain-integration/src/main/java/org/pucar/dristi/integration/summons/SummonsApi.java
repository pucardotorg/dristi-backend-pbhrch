package org.pucar.dristi.integration.summons;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.summons.ChannelMessage;
import org.pucar.dristi.common.contract.summons.ChannelReport;

/**
 * Public, cross-subdomain API of the summons subdomain. Consumed by
 * the {@code epost-tracker} subdomain (delivery-status callback) and
 * any future channel-driver service that needs to push delivery
 * outcomes back to summons.
 *
 * <p>Boundary marker only — peer subdomains MUST call summons through
 * this interface, never reach into {@code internal/}. Enforced by
 * {@code ModuleStructureTest.verify()} at build time.
 *
 * <p>Contract DTOs (HTTP wire format) live in
 * {@code dristi-common/contract/summons/} per Rule 24 (Phase 35 lift).
 */
public interface SummonsApi {

    /**
     * Update delivery status for an in-flight summons. Mirrors the
     * {@code POST /summons/v1/_updateSummons} REST endpoint.
     *
     * <p><b>Rule 35 (cross-subdomain write):</b> exposed deliberately
     * for epost-tracker's delivery-status callback. The downstream
     * Kafka emission ({@code updateSummonsTopic}) that drives Task
     * workflow transitions stays intact; callers see the same
     * acknowledgement-shape they got over REST.
     */
    ChannelMessage updateDeliveryStatus(RequestInfo requestInfo, ChannelReport channelReport);
}
