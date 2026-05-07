/**
 * Lock-svc subdomain — record-level pessimistic locking.
 *
 * <p>Marked as a Spring Modulith application module. Other subdomains
 * MUST consume lock-svc through {@link LockApi}; reaching into
 * {@code internal/} is a structural violation enforced by
 * {@code ModuleStructureTest.verify()}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Lock Service")
package org.pucar.dristi.caselifecycle.locksvc;
