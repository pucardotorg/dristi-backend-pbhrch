/**
 * Order subdomain — court order lifecycle.
 *
 * <p>Marked as a Spring Modulith application module. Other subdomains
 * MUST consume order through {@link OrderApi}; reaching into
 * {@code internal/} is a structural violation enforced by
 * {@code ModuleStructureTest.verify()}.
 *
 * <p>Order's contract DTOs live in
 * {@code dristi-common/contract/order/} (lifted by Phase 35 during the
 * order migration). New methods on {@link OrderApi} should consume
 * those types directly.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Order")
package org.pucar.dristi.caselifecycle.order;
