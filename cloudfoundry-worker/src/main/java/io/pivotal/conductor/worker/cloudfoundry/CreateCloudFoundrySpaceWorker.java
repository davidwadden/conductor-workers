package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;

public class CreateCloudFoundrySpaceWorker implements Worker {

    static final String TASK_DEF_NAME = "create_cloud_foundry_space";

    private final CloudFoundryProperties properties;
    private final CloudFoundrySpaceClient cloudFoundrySpaceClient;

    public CreateCloudFoundrySpaceWorker(
        CloudFoundryProperties properties,
        CloudFoundrySpaceClient cloudFoundrySpaceClient) {
        this.properties = properties;
        this.cloudFoundrySpaceClient = cloudFoundrySpaceClient;
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
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));
        String spaceName = CloudFoundryUtil.deriveSpaceName(projectName, spaceNameSuffix);

        if (!dryRun) {
            String organizationName = properties.getFoundations()
                .get(foundationName)
                .getOrganization();
            cloudFoundrySpaceClient.createSpace(foundationName, organizationName, spaceName);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("spaceName", spaceName);

        return taskResult;
    }
}
