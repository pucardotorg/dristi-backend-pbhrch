package org.pucar.dristi.caselifecycle.task;

import org.pucar.dristi.common.contract.task.Task;
import org.pucar.dristi.common.contract.task.TaskCase;
import org.pucar.dristi.common.contract.task.TaskCaseSearchRequest;
import org.pucar.dristi.common.contract.task.TaskSearchRequest;

import java.util.List;

/**
 * Public, cross-subdomain API of the task subdomain. Other modules
 * (casemanagement today) consume task through this interface — never
 * by importing from {@code internal/}.
 *
 * <p>History: an earlier {@code TaskApi} was introduced and then
 * reverted in commit {@code a89087936} because pairing it with a
 * direct {@code task → cases} edge closed a Spring bean cycle
 * (cases ↔ task ↔ cases via CaseApi). The cycle-break kept
 * task→cases direct and pushed cases→task back to REST. Re-introducing
 * TaskApi is safe here because the new caller (casemanagement) is a
 * sink-node in the dependency graph (no peer subdomain calls into
 * casemanagement), so no cycle can form.
 *
 * <p>Task's contract DTOs live at
 * {@code dristi-common/contract/task/} (lifted by Phase 35).
 */
public interface TaskApi {

    /**
     * Search tasks matching the criteria in the request. Equivalent
     * to the {@code /task/v1/search} REST endpoint's behaviour.
     */
    List<Task> search(TaskSearchRequest request);

    /**
     * Search the case-task projection (table view) matching the
     * criteria in the request. Equivalent to the
     * {@code /task/v1/table/search} REST endpoint.
     */
    List<TaskCase> searchTable(TaskCaseSearchRequest request);
}
