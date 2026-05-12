/**
 * Digitalized-documents subdomain — capture and e-sign of digitalized
 * documents (plea, mediation, examination-of-accused) tied to a case.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. No other subdomain currently
 * consumes digitalized-documents, so there is no public {@code *Api}
 * surface — the {@code @ApplicationModule} marker exists for boundary
 * enforcement (so a future caller is forced to go through a deliberate
 * API rather than reach into {@code internal/}).
 *
 * <p>Outbound: this subdomain consumes
 * {@link org.pucar.dristi.caselifecycle.cases.CaseApi} directly (no
 * REST). The lifted contract DTOs in
 * {@code dristi-common/contract/digitalizeddocuments/} carry the HTTP
 * wire format for the subdomain's own controller endpoints.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Digitalizeddocuments")
package org.pucar.dristi.caselifecycle.digitalizeddocuments;
