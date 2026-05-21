package org.pucar.dristi.caselifecycle.task;

import org.pucar.dristi.common.contract.task.Task;
import org.pucar.dristi.common.contract.task.TaskRequest;
import org.pucar.dristi.common.contract.task.TaskSearchRequest;

import java.util.List;

/**
 * Public, cross-subdomain API of the task subdomain. Consumed by
 * order-management today.
 *
 * <p><b>Cycle history:</b> a TaskApi was deliberately removed in commit
 * {@code a89087936} during the task migration's C3 cycle-break: the
 * {@code cases→task→order→cases} Spring DI loop was severed by reverting
 * the {@code cases→task} edge to REST. This re-introduction is safe
 * because (a) order-management is downstream-only — no edge feeds back
 * to it from any subdomain, and (b) the original cycle remains broken:
 * cases continues to call task via REST. Only order-management consumes
 * TaskApi.
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
}
