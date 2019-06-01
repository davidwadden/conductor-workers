package io.pivotal.conductor.worker.template;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;

public class InterpolateConcoursePipelineWorker implements Worker {

    static final String TASK_DEF_NAME = "interpolate_concourse_pipeline_worker";

    private final TemplateProperties properties;
    private final Resource templateYamlResource;

    public InterpolateConcoursePipelineWorker(TemplateProperties properties,
        Resource templateYamlResource) {
        this.properties = properties;
        this.templateYamlResource = templateYamlResource;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String projectName = (String) task.getInputData().get("projectName");
        Map<String, Object> config = (Map<String, Object>) task.getInputData().get("config");

        List<String> lines;
        try {
            lines = Files.readAllLines(templateYamlResource.getFile().toPath())
                .stream()
                .map((line) -> line.replace("{{ project-name }}", projectName))
                .map((line) -> line.replace("{{ cf-username }}", (String) config.get("cf-username")))
                .map((line) -> line.replace("{{ cf-password }}", (String) config.get("cf-password")))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String pipelineYaml = String.join("\n", lines);

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("pipelineYaml", pipelineYaml);

        return taskResult;
    }

}
