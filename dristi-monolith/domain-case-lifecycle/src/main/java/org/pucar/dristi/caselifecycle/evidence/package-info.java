/**
 * Evidence subdomain — artifacts and evidence-record lifecycle (witness
 * depositions, signed PDFs, vakalatnamas, mediation documents, etc).
 *
 * <p>Marked as a Spring Modulith application module. Other subdomains
 * MUST consume evidence through {@link EvidenceApi}; reaching into
 * {@code internal/} is a structural violation enforced by
 * {@code ModuleStructureTest.verify()}.
 *
 * <p>Evidence's contract DTOs live in
 * {@code dristi-common/contract/evidence/} (lifted by Phase 35 during
 * the evidence migration). New methods on {@link EvidenceApi} should
 * consume those types directly.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Evidence")
@org.springframework.modulith.NamedInterface("api")
package org.pucar.dristi.caselifecycle.evidence;
