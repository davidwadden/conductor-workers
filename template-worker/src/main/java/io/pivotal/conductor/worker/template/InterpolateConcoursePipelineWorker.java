package io.pivotal.conductor.worker.template;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import io.pivotal.conductor.lib.template.FreemarkerTemplateProcessor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.springframework.core.io.Resource;

public class InterpolateConcoursePipelineWorker implements Worker {

    static final String TASK_DEF_NAME = "interpolate_concourse_pipeline";

    private final TemplateProperties properties;
    private final Resource templateYamlResource;
    private final FreemarkerTemplateProcessor freemarkerTemplateProcessor;

    public InterpolateConcoursePipelineWorker(TemplateProperties properties,
        Resource templateYamlResource,
        FreemarkerTemplateProcessor freemarkerTemplateProcessor) {
        this.properties = properties;
        this.templateYamlResource = templateYamlResource;
        this.freemarkerTemplateProcessor = freemarkerTemplateProcessor;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String projectName = (String) task.getInputData().get("projectName");
        Map<String, Object> templateParams = (Map<String, Object>) task.getInputData().get("templateParams");

        String pipelineYaml;
        try {
            byte[] templateYamlBytes = Files.readAllBytes(Paths.get(templateYamlResource.getFile().toURI()));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(templateYamlBytes);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            freemarkerTemplateProcessor.process(inputStream, outputStream, templateParams);

            pipelineYaml = outputStream.toString(Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("pipelineYaml", pipelineYaml);

        return taskResult;
    }

}
