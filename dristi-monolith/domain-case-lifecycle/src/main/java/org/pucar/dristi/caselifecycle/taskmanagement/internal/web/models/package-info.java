/**
 * Taskmanagement contract DTOs — HTTP wire format types for the
 * taskmanagement subdomain's public API surface.
 *
 * <p>Exposed cross-subdomain via {@link org.springframework.modulith.NamedInterface}
 * so other modules (hearing today) can consume them via
 * {@link org.pucar.dristi.caselifecycle.taskmanagement.TaskmanagementApi}
 * without Spring Modulith flagging "depends on non-exposed type" violations.
 *
 * <p>These types live here (not in
 * {@code dristi-common/contract/taskmanagement/}) because taskmanagement's
 * web/models tree carries cross-package references into {@code cases/},
 * {@code enums/}, {@code taskdetails/}, and {@code PaymentCalculator/}
 * sub-packages that Phase 35's retro-lift cannot strip cleanly today.
 * Mirrors the {@code cases} subdomain's precedent.
 */
@org.springframework.modulith.NamedInterface("contract")
package org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models;
