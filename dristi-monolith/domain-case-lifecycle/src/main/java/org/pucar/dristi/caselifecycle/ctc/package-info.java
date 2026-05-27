/**
 * CTC (Certified True Copy) subdomain — application and document workflow.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. Other subdomains MUST consume
 * ctc through {@link CtcApi}; reaching into {@code internal/} is a
 * structural violation. Writes (e.g. CTC application update) remain
 * on REST per Rule 35 until a deliberate design pass on cross-subdomain
 * mutators.
 *
 * <p>Ctc's contract DTOs live at
 * {@code dristi-common/contract/ctc/} (lifted by Phase 35).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Ctc")
@org.springframework.modulith.NamedInterface("api")
package org.pucar.dristi.caselifecycle.ctc;
