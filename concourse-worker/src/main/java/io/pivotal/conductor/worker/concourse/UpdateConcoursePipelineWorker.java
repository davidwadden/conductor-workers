package io.pivotal.conductor.worker.concourse;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestOperations;

public class UpdateConcoursePipelineWorker implements Worker {

    static final String TASK_DEF_NAME = "update_concourse_pipeline";

    private final ConcourseProperties properties;
    private final RestOperations restOperations;

    public UpdateConcoursePipelineWorker(ConcourseProperties properties,
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
        String pipelineYaml = (String) task.getInputData().get("pipelineYaml");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));
        String pipelineName = ConcoursePipelineUtil.derivePipelineName(projectName);
        String pipelineUrl = String.format("%s/teams/%s/pipelines/%s",
            properties.getApiHost(), properties.getTeamName(), pipelineName);

        if (!dryRun) {
            String requestUrl =
                String.format("%s/api/v1/teams/%s/pipelines/%s/config", properties.getApiHost(),
                    properties.getTeamName(), pipelineName);

            RequestEntity requestEntity = RequestEntity
                .put(URI.create(requestUrl))
                .contentType(MediaType.parseMediaType("application/x-yaml"))
                .body(pipelineYaml);

            restOperations.exchange(requestEntity, Void.class);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("pipelineName", pipelineName);
        // TODO: Return 201 Created Location header as outputData["pipelineUrl"]
        taskResult.getOutputData().put("pipelineUrl", pipelineUrl);

        return taskResult;
    }

}
