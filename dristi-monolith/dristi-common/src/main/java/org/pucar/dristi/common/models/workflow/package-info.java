/**
 * DRISTI's standard extensions over egov's workflow contract:
 * {@link WorkflowObject} and {@link ProcessInstanceObject} both add
 * an {@code additionalDetails} field that every DRISTI service includes
 * in its workflow transition payloads.
 *
 * <p>Exposed as a {@link org.springframework.modulith.NamedInterface} so
 * domain modules can depend on these types without crossing the
 * {@code dristi-common} boundary.
 */
@org.springframework.modulith.NamedInterface("models-workflow")
package org.pucar.dristi.common.models.workflow;
