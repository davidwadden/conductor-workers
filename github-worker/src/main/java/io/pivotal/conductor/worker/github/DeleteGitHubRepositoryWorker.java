package io.pivotal.conductor.worker.github;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestOperations;

public class DeleteGitHubRepositoryWorker implements Worker {

    static final String TASK_DEF_NAME = "delete_github_repository";

    private final GitHubProperties properties;
    private final RestOperations restOperations;

    public DeleteGitHubRepositoryWorker(GitHubProperties properties,
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
        String repositoryName = GitHubRepositoryUtil.deriveRepositoryName(projectName);

        String requestUrl = String.format("https://api.github.com/repos/%s/%s",
            properties.getOrganizationName(), repositoryName);

        RequestEntity<Void> requestEntity = RequestEntity
            .delete(URI.create(requestUrl))
            .header(HttpHeaders.AUTHORIZATION, "token " + properties.getToken())
            .header(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
            .build();

        Boolean wasDeleted = false;
        if (!dryRun) {
            restOperations.exchange(requestEntity, Void.class);
            wasDeleted = true;
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("wasDeleted", wasDeleted);

        return taskResult;
    }
}
