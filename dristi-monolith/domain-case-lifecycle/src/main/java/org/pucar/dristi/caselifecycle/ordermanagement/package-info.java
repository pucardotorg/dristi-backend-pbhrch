/**
 * Order-management subdomain — order workflow, BSS e-sign coordination,
 * cron-driven mandatory-submission and process-payment reminders,
 * draft-order generation, BOTD (business-of-the-day) listings.
 *
 * <p>Marked as a Spring Modulith application module. Other subdomains
 * MUST NOT reach into {@code internal/}; structural violations are
 * enforced by {@code ModuleStructureTest.verify()}. No
 * {@code OrdermanagementApi} is exposed yet — no in-tree consumers
 * have surfaced.
 *
 * <p>Order-management's contract DTOs live in
 * {@code dristi-common/contract/ordermanagement/} (lifted by Phase 35
 * during the order-management migration).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Ordermanagement")
package org.pucar.dristi.caselifecycle.ordermanagement;
