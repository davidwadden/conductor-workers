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
        String username = (String) task.getInputData().get("username");
        String password = (String) task.getInputData().get("password");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        if (!dryRun) {
            cloudFoundryUserClient.createUser(foundationName, username, password);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);

        return taskResult;
    }
}
