package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudfoundry.uaa.users.UserId;

public class ListCloudFoundryUsersWorker implements Worker {

    static final String TASK_DEF_NAME = "list_cloud_foundry_users";

    private final CloudFoundryUserClient cloudFoundryUserClient;

    public ListCloudFoundryUsersWorker(
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
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        Collection<String> userNames = new ArrayList<>();
        if (!dryRun) {
            Collection<UserId> userIds = cloudFoundryUserClient.listUsers(foundationName, userName);
            userNames = userIds.stream()
                .map(UserId::getUserName)
                .collect(Collectors.toList());
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.addOutputData("userCount", userNames.size());
        taskResult.addOutputData("userNames", userNames);

        return taskResult;
    }
}
