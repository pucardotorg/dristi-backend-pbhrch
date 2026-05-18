/**
 * Treasury subdomain — e-treasury payment orchestration, demand creation,
 * and receipt retrieval.
 *
 * <p>Marked as a Spring Modulith application module. Other subdomains
 * MUST consume treasury through {@link org.pucar.dristi.integration.treasury.TreasuryApi};
 * reaching into {@code internal/} is a structural violation enforced by
 * {@code ModuleStructureTest.verify()}.
 *
 * <p>Exposed as a named interface so sibling top-level modules
 * (e.g. {@code caselifecycle}) can wire against {@code TreasuryApi}
 * without Spring Modulith flagging a "non-exposed type" violation.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.modulith.ApplicationModule(displayName = "Treasury")
package org.pucar.dristi.integration.treasury;
