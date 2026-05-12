/**
 * Advocate subdomain — advocate and advocate-clerk registration.
 *
 * <p>Marked as a Spring Modulith application module. Other subdomains
 * MUST consume advocate through {@link org.pucar.dristi.identityaccess.advocate.AdvocateApi};
 * reaching into {@code internal/} is a structural violation enforced by
 * {@code ModuleStructureTest.verify()}.
 *
 * <p>Exposed as a named interface so sibling top-level modules
 * (e.g. {@code caselifecycle}) can wire against {@code AdvocateApi}
 * without Spring Modulith flagging a "non-exposed type" violation.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.modulith.ApplicationModule(displayName = "Advocate")
package org.pucar.dristi.identityaccess.advocate;
