package io.pivotal.conductor.worker.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import io.pivotal.conductor.lib.template.FreemarkerTemplateProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

@ExtendWith(MockitoExtension.class)
class InterpolateConcoursePipelineWorkerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    private TemplateProperties properties;
    private ClassPathResource templateYamlResource;
    @Mock
    private FreemarkerTemplateProcessor mockFreemarkerTemplateProcessor;
    @Captor
    private ArgumentCaptor<InputStream> inputStreamCaptor;
    @Captor
    private ArgumentCaptor<Map<String, Object>> dataModelCaptor;
    private InterpolateConcoursePipelineWorker worker;

    @BeforeEach
    void setUp() {
        properties = new TemplateProperties();
        templateYamlResource = new ClassPathResource("/fixtures/pipeline-template.yml.ftl");
        worker = new InterpolateConcoursePipelineWorker(properties, templateYamlResource,
            mockFreemarkerTemplateProcessor);
    }

    @Test
    void execute() throws IOException {

        String gitRepositoryUrl = "https://some-git-server/some-organization/some-repository";
        Map<String, Object> templateParams = ImmutableMap.of(
            "projectName", "Some Project Name!",
            "gitRepositoryUrl", gitRepositoryUrl
        );

        String templateYaml = copyResourceToString(templateYamlResource);
        String expectedOutput = templateYaml
            .replace("${projectName}", (String) templateParams.get("projectName"))
            .replace("${gitRepositoryUrl}", (String) templateParams.get("gitRepositoryUrl"));

        doAnswer(
            invocation -> {
                InputStream inputStream = invocation.getArgument(0);
                OutputStream outputStream = invocation.getArgument(1);
                Map<String, Object> dataModel = invocation.getArgument(2);

                outputStream.write(expectedOutput.getBytes());
                return null;
            })
            .when(mockFreemarkerTemplateProcessor)
            .process(any(), any(), any());

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of(
            "projectName", "Some Project Name!",
            "templateParams", templateParams
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockFreemarkerTemplateProcessor)
            .process(inputStreamCaptor.capture(), any(), dataModelCaptor.capture());
        assertThat(inputStreamCaptor.getValue()).hasContent(templateYaml);
        assertThat(dataModelCaptor.getValue()).containsAllEntriesOf(templateParams);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);

        String pipelineYaml = (String) taskResult.getOutputData().get("pipelineYaml");
        JsonNode pipelineYamlNode = objectMapper.readTree(pipelineYaml);

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
