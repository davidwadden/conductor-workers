package io.pivotal.conductor.worker.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

class InterpolateConcoursePipelineWorkerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    private TemplateProperties properties;
    private ClassPathResource templateYamlResource;
    private InterpolateConcoursePipelineWorker worker;

    @BeforeEach
    void setUp() {
        properties = new TemplateProperties();
        templateYamlResource = new ClassPathResource("/pipeline-template.yml");
        worker = new InterpolateConcoursePipelineWorker(properties, templateYamlResource);
    }

    @Test
    void execute() throws IOException {
        String gitRepositoryUrl = "https://some-git-server/some-organization/some-repository";
        Map<String, Object> templateParams = Map.of(
            "project-name", "Some Project Name!",
            "git-repository-url", gitRepositoryUrl
        );

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = Map.of(
            "projectName", "Some Project Name!",
            "templateParams", templateParams
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);

        String pipelineYaml = (String) taskResult.getOutputData().get("pipelineYaml");
        JsonNode pipelineYamlNode = objectMapper.readTree(pipelineYaml);

        String templateYaml = copyResourceToString(templateYamlResource);
        ObjectNode templateYamlNode = (ObjectNode) objectMapper.readTree(templateYaml);
        ObjectNode gitResourceSourceNode = (ObjectNode) templateYamlNode
            .withArray("resources")
            .elements()
            .next()
            .with("source");
        gitResourceSourceNode.set("uri", TextNode.valueOf(gitRepositoryUrl));

        assertThat(pipelineYaml).containsPattern(Pattern.compile("^## Some Project Name!.*"));
        assertThat(pipelineYamlNode).isEqualTo(templateYamlNode);
    }

    private String copyResourceToString(ClassPathResource classPathResource) {
        String pipelineYaml;
        try (Reader reader = new InputStreamReader(classPathResource.getInputStream())) {
            pipelineYaml = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pipelineYaml;
    }

}
