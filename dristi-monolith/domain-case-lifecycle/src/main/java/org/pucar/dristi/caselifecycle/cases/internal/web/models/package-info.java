/**
 * Case contract DTOs — HTTP wire format types for the case subdomain's
 * public API surface.
 *
 * <p>Exposed cross-subdomain via {@link org.springframework.modulith.NamedInterface}
 * so other modules (order, hearing once migrated, etc.) can consume them
 * via {@link org.pucar.dristi.caselifecycle.cases.CaseApi} without
 * Spring Modulith flagging "depends on non-exposed type" violations.
 *
 * <p>These types live here (not in
 * {@code dristi-common/contract/cases/}) because case's web/models
 * carries persistence + validator tendrils that Phase 35's retro-lift
 * cannot strip cleanly today. The eventual relocation is documented in
 * {@code scripts/migration/FOLLOWUP_RETROLIFT_PATH_A.md}; until then,
 * this named-interface marker keeps the cross-module boundary clean.
 */
@org.springframework.modulith.NamedInterface("contract")
package org.pucar.dristi.caselifecycle.cases.internal.web.models;
