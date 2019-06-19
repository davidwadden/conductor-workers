package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;

public class CreateCloudFoundryServiceInstanceWorker implements Worker {

    static final String TASK_DEF_NAME = "create_cloud_foundry_service_instance";

    private final CloudFoundryServiceClient cloudFoundryServiceClient;

    public CreateCloudFoundryServiceInstanceWorker(
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
        String serviceName = (String) task.getInputData().get("serviceName");
        String servicePlanName = (String) task.getInputData().get("servicePlanName");
        String serviceInstanceName = (String) task.getInputData().get("serviceInstanceName");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        if (!dryRun) {
            cloudFoundryServiceClient
                .createServiceInstance(foundationName, organizationName, spaceName, serviceName,
                    servicePlanName, serviceInstanceName);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);

        return taskResult;
    }
}
