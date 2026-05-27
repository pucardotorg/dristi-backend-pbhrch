/**
 * Digitalized-documents subdomain — capture and e-sign of digitalized
 * documents (plea, mediation, examination-of-accused) tied to a case.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. Consumers (order-management
 * today) MUST go through
 * {@link org.pucar.dristi.caselifecycle.digitalizeddocuments.DigitalizedDocumentsApi};
 * reaching into {@code internal/} is a structural violation.
 *
 * <p>Outbound: this subdomain consumes
 * {@link org.pucar.dristi.caselifecycle.cases.CaseApi} directly (no
 * REST). Contract DTOs live at
 * {@code dristi-common/contract/digitalizeddocuments/} (Phase-35
 * lifted).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Digitalizeddocuments")
@org.springframework.modulith.NamedInterface("api")
package org.pucar.dristi.caselifecycle.digitalizeddocuments;
