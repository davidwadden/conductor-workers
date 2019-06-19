package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;

public class DeleteCloudFoundryServiceInstanceWorker implements Worker {

    static final String TASK_DEF_NAME = "delete_cloud_foundry_service_instance";

    private final CloudFoundryServiceClient cloudFoundryServiceClient;

    public DeleteCloudFoundryServiceInstanceWorker(
        CloudFoundryServiceClient cloudFoundryServiceClient) {
        this.cloudFoundryServiceClient = cloudFoundryServiceClient;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String foundationName = (String) task.getInputData().get("foundationName");
        String organizationName = (String) task.getInputData().get("organizationName");
        String spaceName = (String) task.getInputData().get("spaceName");
        String serviceInstanceName = (String) task.getInputData().get("serviceInstanceName");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        if (!dryRun) {
            cloudFoundryServiceClient
                .deleteServiceInstance(foundationName, organizationName, spaceName,
                    serviceInstanceName);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);

        return taskResult;
    }
}
