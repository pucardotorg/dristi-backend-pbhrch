/**
 * Bailbond subdomain — surety / bail-bond registration, encryption, e-sign,
 * and notification flows.
 *
 * <p>Marked as a Spring Modulith application module. No cross-subdomain
 * callers consume bailbond today; the marker enforces the boundary so a
 * future caller cannot reach into {@code internal/}. Bailbond itself
 * consumes {@code cases.CaseApi} for case-detail lookups (Rule 32 direct
 * call; no REST).
 *
 * <p>Contract DTOs (HTTP wire format) live in
 * {@code dristi-common/contract/bailbond/} (lifted by Phase 35 of the
 * per-module migration pipeline), exposed via
 * {@code @NamedInterface("contract-bailbond")}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Bailbond")
package org.pucar.dristi.caselifecycle.bailbond;
