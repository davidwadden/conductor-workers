package io.pivotal.conductor.worker.bitbucket;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestOperations;

public class DeleteBitbucketRepositoryWorker implements Worker {

    static final String TASK_DEF_NAME = "delete_bitbucket_repository";

    private final BitbucketProperties properties;
    private final RestOperations restOperations;

    public DeleteBitbucketRepositoryWorker(BitbucketProperties properties,
        RestOperations restOperations) {
        this.properties = properties;
        this.restOperations = restOperations;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String projectName = (String) task.getInputData().get("projectName");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        Boolean wasDeleted = false;
        if (!dryRun) {
            String repositoryName = deriveRepositoryName(projectName);
            String requestUrl =
                String.format("https://api.bitbucket.org/2.0/repositories/%s/%s",
                    properties.getTeamName(), repositoryName);

            RequestEntity<Void> requestEntity = RequestEntity
                .delete(URI.create(requestUrl))
                .build();

            restOperations.exchange(requestEntity, Void.class);
            wasDeleted = true;
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("wasDeleted", wasDeleted);

        return taskResult;
    }

    private static String deriveRepositoryName(String projectName) {

        return projectName
            .replaceAll("[^\\w\\s]", "")
            .replaceAll("[\\s]", "-")
            .replace("_", "-")
            .replace(".", "-")
            .replaceAll("[^a-zA-Z0-9\\-]", "")
            .toLowerCase();
    }

}
