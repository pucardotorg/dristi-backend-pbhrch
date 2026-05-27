/**
 * Ab-diary subdomain — bulk case-diary entry generation for A-diary
 * (live) and B-diary (deferred). Other subdomains MUST consume ab-diary
 * through {@link org.pucar.dristi.caselifecycle.abdiary.AbdiaryApi};
 * reaching into {@code internal/} is a structural violation enforced
 * by {@code ModuleStructureTest.verify()}.
 *
 * <p>Contract DTOs live at {@code dristi-common/contract/abdiary/}.
 */
@NamedInterface
@ApplicationModule(displayName = "Abdiary")
package org.pucar.dristi.caselifecycle.abdiary;

import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.NamedInterface;
