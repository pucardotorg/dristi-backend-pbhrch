/**
 * CTC (Certified True Copy) subdomain — application and document workflow.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. No other migrated subdomain
 * calls into ctc yet, so no {@code CtcApi} interface is needed.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Ctc")
package org.pucar.dristi.caselifecycle.ctc;
