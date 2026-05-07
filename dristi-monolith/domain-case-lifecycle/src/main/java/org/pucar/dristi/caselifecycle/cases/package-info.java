/**
 * Cases subdomain — case lifecycle persistence and orchestration.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} (cases, locksvc, order,
 * notification) are enforced by {@code ModuleStructureTest.verify()}.
 * Other subdomains MUST consume cases through {@link CaseApi}; reaching
 * into {@code internal/} is a structural violation.
 *
 * <p>Contract DTOs (HTTP wire format) live in
 * {@code internal/web/models/} and are exposed cross-subdomain via the
 * {@code @NamedInterface("contract")} marker on that package's
 * {@code package-info.java}. Eventual relocation to
 * {@code dristi-common/contract/cases/} is tracked in
 * {@link scripts/migration/FOLLOWUP_RETROLIFT_PATH_A.md}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Case")
package org.pucar.dristi.caselifecycle.cases;
