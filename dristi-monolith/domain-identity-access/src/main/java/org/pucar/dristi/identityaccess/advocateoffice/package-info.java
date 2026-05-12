/**
 * AdvocateOffice subdomain — advocate office member management.
 *
 * <p>Marked as a Spring Modulith application module. Other subdomains
 * MUST consume advocate-office through {@link org.pucar.dristi.identityaccess.advocateoffice.AdvocateOfficeApi};
 * reaching into {@code internal/} is a structural violation enforced by
 * {@code ModuleStructureTest.verify()}.
 *
 * <p>Exposed as a named interface so sibling top-level modules
 * (e.g. {@code caselifecycle}) can wire against {@code AdvocateOfficeApi}
 * without Spring Modulith flagging a "non-exposed type" violation.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.modulith.ApplicationModule(displayName = "AdvocateOffice")
package org.pucar.dristi.identityaccess.advocateoffice;
