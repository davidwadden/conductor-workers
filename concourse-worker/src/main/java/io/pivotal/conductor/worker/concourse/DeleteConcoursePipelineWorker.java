package io.pivotal.conductor.worker.concourse;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

public class DeleteConcoursePipelineWorker implements Worker {

    static final String TASK_DEF_NAME = "delete_concourse_pipeline";

    private final ConcourseProperties properties;
    private final RestOperations restOperations;

    public DeleteConcoursePipelineWorker(
        ConcourseProperties properties,
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
            deletePipeline(projectName);
            wasDeleted = true;
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.getOutputData().put("wasDeleted", wasDeleted);
        taskResult.setStatus(Status.COMPLETED);

        return taskResult;
    }

    private void deletePipeline(String projectName) {
        String pipelineName = ConcoursePipelineUtil.derivePipelineName(projectName);
        destroyPipeline(pipelineName);
    }

    private void destroyPipeline(String pipelineName) {
        String requestUrl =
            String.format("%s/api/v1/teams/%s/pipelines/%s", properties.getApiHost(),
                properties.getTeamName(), pipelineName);

        RequestEntity requestEntity = RequestEntity
            .delete(URI.create(requestUrl))
            .build();

        ResponseEntity<Void> responseEntity = restOperations.exchange(requestEntity, Void.class);

        if (responseEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
            String exceptionMessage = String.format("failed to delete %s: status=%s",
                requestUrl, responseEntity.getStatusCode());
            throw new RuntimeException(exceptionMessage);
        }
    }
}
