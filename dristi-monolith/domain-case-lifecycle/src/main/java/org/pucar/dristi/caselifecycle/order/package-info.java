/**
 * Order subdomain — court order lifecycle.
 *
 * <p>Marked as a Spring Modulith application module. Cross-subdomain callers
 * consume order through {@code org.pucar.dristi.common.order.OrderApi}
 * (in dristi-common); reaching into {@code internal/} is a structural
 * violation enforced by {@code ModuleStructureTest.verify()}.
 *
 * <p>Order's contract DTOs live in
 * {@code dristi-common/contract/order/} (lifted by Phase 35 during the
 * order migration).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Order")
package org.pucar.dristi.caselifecycle.order;
