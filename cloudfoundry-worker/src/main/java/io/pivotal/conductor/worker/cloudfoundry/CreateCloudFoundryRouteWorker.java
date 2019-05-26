package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;

public class CreateCloudFoundryRouteWorker implements Worker {

    static final String TASK_DEF_NAME = "create_cloud_foundry_route";

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String projectName = (String) task.getInputData().get("projectName");
        String hostnameSuffix = (String) task.getInputData().get("hostnameSuffix");
        String spaceName = (String) task.getInputData().get("spaceName");
        String hostname = CloudFoundryUtil.deriveRouteHostname(projectName, hostnameSuffix);

        // create route on cloud foundry

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("hostname", hostname);

        return taskResult;
    }
}
