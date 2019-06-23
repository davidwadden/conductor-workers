package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;

public class CreateCloudFoundryUserWorker implements Worker {

    static final String TASK_DEF_NAME = "create_cloud_foundry_user";

    private final CloudFoundryUserClient cloudFoundryUserClient;

    public CreateCloudFoundryUserWorker(
        CloudFoundryUserClient cloudFoundryUserClient) {
        this.cloudFoundryUserClient = cloudFoundryUserClient;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String foundationName = (String) task.getInputData().get("foundationName");
        String userName = (String) task.getInputData().get("userName");
        String password = (String) task.getInputData().get("password");
        String origin = (String) task.getInputData().get("origin");
        String externalId = (String) task.getInputData().get("externalId");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        if (!dryRun) {
            cloudFoundryUserClient
                .createUser(foundationName, userName, password, origin, externalId);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);

        return taskResult;
    }
}
