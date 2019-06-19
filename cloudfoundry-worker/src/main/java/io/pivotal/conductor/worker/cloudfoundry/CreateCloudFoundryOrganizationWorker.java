package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;

public class CreateCloudFoundryOrganizationWorker implements Worker {

    static final String TASK_DEF_NAME = "create_cloud_foundry_organization";

    private final CloudFoundryOrganizationClient organizationClient;

    public CreateCloudFoundryOrganizationWorker(
        CloudFoundryOrganizationClient organizationClient) {
        this.organizationClient = organizationClient;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String foundationName = (String) task.getInputData().get("foundationName");
        String organizationName = (String) task.getInputData().get("organizationName");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        if (!dryRun) {
            organizationClient.createOrganization(foundationName, organizationName);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);

        return taskResult;
    }
}
