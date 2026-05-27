/**
 * HearingManagement subdomain — inbox and pending-task query facade.
 *
 * <p>Currently a bridge: the implementation wraps the external egov-inbox
 * REST service until the subdomain is fully migrated. Cross-subdomain callers
 * consume this via {@code org.pucar.dristi.common.hearingmanagement.HearingManagementApi}
 * in dristi-common; reaching into {@code internal/} is a structural violation.
 */
@org.springframework.modulith.ApplicationModule(displayName = "HearingManagement")
package org.pucar.dristi.caselifecycle.hearingmanagement;
