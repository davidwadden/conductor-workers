package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;

public class DeleteCloudFoundrySpaceWorker implements Worker {

    static final String TASK_DEF_NAME = "delete_cloud_foundry_space";

    private final CloudFoundrySpaceClient cloudFoundrySpaceClient;

    public DeleteCloudFoundrySpaceWorker(CloudFoundrySpaceClient cloudFoundrySpaceClient) {
        this.cloudFoundrySpaceClient = cloudFoundrySpaceClient;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String projectName = (String) task.getInputData().get("projectName");
        String spaceNameSuffix = (String) task.getInputData().get("spaceNameSuffix");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));
        String spaceName = CloudFoundryUtil.deriveSpaceName(projectName, spaceNameSuffix);

        Boolean wasDeleted = false;
        if (!dryRun) {
            wasDeleted = cloudFoundrySpaceClient.deleteSpace(spaceName);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("wasDeleted", wasDeleted);

        return taskResult;
    }
}
