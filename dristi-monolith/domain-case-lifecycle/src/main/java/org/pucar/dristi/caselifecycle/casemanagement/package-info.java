/**
 * Casemanagement subdomain — case-bundle generation, summons PDF
 * synthesis, and case-manager facing search/history/summary endpoints.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. No other subdomain currently
 * consumes casemanagement, so there is no public {@code *Api} surface —
 * the {@code @ApplicationModule} marker exists for boundary enforcement
 * (so a future caller is forced to go through a deliberate API rather
 * than reach into {@code internal/}).
 *
 * <p>Outbound: this subdomain consumes
 * {@link org.pucar.dristi.caselifecycle.cases.CaseApi} and
 * {@link org.pucar.dristi.caselifecycle.taskmanagement.TaskmanagementApi}
 * directly (no REST). The {@code task} subdomain has no {@code *Api}
 * (reverted during the cases↔task↔order cycle-break in commit
 * {@code a89087936}); calls into {@code task} remain on REST. The
 * lifted contract DTOs in {@code dristi-common/contract/casemanagement/}
 * carry the HTTP wire format for the subdomain's own controller
 * endpoints.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Casemanagement")
package org.pucar.dristi.caselifecycle.casemanagement;
