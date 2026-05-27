package org.pucar.dristi.caselifecycle.task;

import org.pucar.dristi.common.contract.task.Task;
import org.pucar.dristi.common.contract.task.TaskCase;
import org.pucar.dristi.common.contract.task.TaskCaseSearchRequest;
import org.pucar.dristi.common.contract.task.TaskRequest;
import org.pucar.dristi.common.contract.task.TaskSearchRequest;

import java.util.List;

/**
 * Public, cross-subdomain API of the task subdomain. Consumed by
 * casemanagement and order-management today.
 *
 * <p><b>Cycle history:</b> a TaskApi was deliberately removed in commit
 * {@code a89087936} during the task migration's C3 cycle-break: the
 * {@code cases→task→order→cases} Spring DI loop was severed by reverting
 * the {@code cases→task} edge to REST. This re-introduction is safe
 * because the active callers — order-management and casemanagement —
 * are downstream-only / sink-nodes (no peer subdomain calls back into
 * them), so no cycle can form. The original cycle remains broken:
 * cases continues to call task via REST.
 *
 * <p>Contract DTOs live at {@code dristi-common/contract/task/}.
 */
public interface TaskApi {

    /**
     * Search tasks matching the criteria + pagination in the request.
     * Mirrors the {@code /task/v1/_search} REST endpoint.
     */
    List<Task> search(TaskSearchRequest request);

    /**
     * Create a new task — mirrors {@code /task/v1/_create}.
     */
    Task create(TaskRequest request);

    /**
     * Update an existing task — mirrors {@code /task/v1/_update}.
     */
    Task update(TaskRequest request);

    /**
     * Search the case-task projection (table view) matching the
     * criteria in the request. Equivalent to the
     * {@code /task/v1/table/search} REST endpoint.
     */
    List<TaskCase> searchTable(TaskCaseSearchRequest request);
}
