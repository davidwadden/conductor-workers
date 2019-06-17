package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;

public class CreateRabbitMqServiceWorker implements Worker {

    static final String TASK_DEF_NAME = "create_rabbitmq";

    private final CloudFoundryProperties properties;
    private final CloudFoundryServiceClient cloudFoundryServiceClient;

    public CreateRabbitMqServiceWorker(
        CloudFoundryProperties properties,
        CloudFoundryServiceClient cloudFoundryServiceClient) {
        this.properties = properties;
        this.cloudFoundryServiceClient = cloudFoundryServiceClient;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String foundationName = (String) task.getInputData().get("foundationName");
        String projectName = (String) task.getInputData().get("projectName");
        String spaceNameSuffix = (String) task.getInputData().get("spaceNameSuffix");
        String spaceName = (String) task.getInputData().get("spaceName");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));
        String serviceInstanceName = CloudFoundryUtil.deriveAmqpName(projectName, spaceNameSuffix);

        if (!dryRun) {
            String organizationName = properties.getFoundations()
                .get(foundationName)
                .getOrganization();
            cloudFoundryServiceClient.createRabbitMqBroker(
                foundationName, organizationName, spaceName, serviceInstanceName);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("serviceInstanceName", serviceInstanceName);

        return taskResult;
    }
}
