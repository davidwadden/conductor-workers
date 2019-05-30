package io.pivotal.conductor.worker.concourse;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestOperations;

public class ExposeConcoursePipelineWorker implements Worker {

    static final String TASK_DEF_NAME = "expose_concourse_pipeline";

    private final ConcourseProperties properties;
    private final RestOperations restOperations;

    public ExposeConcoursePipelineWorker(ConcourseProperties properties,
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
        String pipelineName = ConcoursePipelineUtil.derivePipelineName(projectName);

        if (!dryRun) {
            String requestUrl =
                String.format("%s/api/v1/teams/%s/pipelines/%s/expose", properties.getApiHost(),
                    properties.getTeamName(), pipelineName);

            RequestEntity requestEntity = RequestEntity
                .put(URI.create(requestUrl))
                .build();

            restOperations.exchange(requestEntity, Void.class);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);

        return taskResult;
    }

}
