package io.pivotal.conductor.worker.concourse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.FileCopyUtils;

class UpdateConcoursePipelineWorkerTest {

    private ConcourseProperties properties;
    private MockRestServiceServer mockServer;
    private UpdateConcoursePipelineWorker worker;

    @BeforeEach
    void setUp() {
        properties = new ConcourseProperties();
        ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resourceDetails);
        restTemplate.setAccessTokenProvider(new FakeAccessTokenProvider());
        mockServer = MockRestServiceServer.createServer(restTemplate);
        worker = new UpdateConcoursePipelineWorker(properties, restTemplate);
    }

    @Test
    void execute() {
        properties.setApiHost("https://some-api-host");
        properties.setTeamName("some-team-name");
        properties.setUsername("some-username");
        properties.setPassword("some-password");

        String requestUrl =
            String.format("%s/api/v1/teams/%s/pipelines/some-project-name/config",
                properties.getApiHost(), properties.getTeamName());
        Resource pipelineYamlResource = new ClassPathResource("/sample-pipeline.yml");
        String pipelineYaml = copyResourceToString(pipelineYamlResource);

        mockServer
            .expect(requestTo(requestUrl))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().contentType("application/x-yaml"))
            .andExpect(content().string(equalTo(pipelineYaml)))
            .andRespond(withCreatedEntity(URI.create("https://fake-pipeline-url")));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = Map.of(
            "projectName", "Some Project Name!",
            "pipelineYaml", pipelineYaml
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);

        String pipelineName = ConcoursePipelineUtil.derivePipelineName("Some Project Name!");
        String pipelineUrl = String.format("%s/teams/%s/pipelines/%s",
            properties.getApiHost(), properties.getTeamName(), pipelineName);
        assertThat(taskResult.getOutputData()).containsEntry("pipelineUrl", pipelineUrl);
    }

    @Test
    void dryRun() {
        properties.setApiHost("https://some-api-host");
        properties.setTeamName("some-team-name");
        properties.setUsername("some-username");
        properties.setPassword("some-password");

        Resource pipelineYamlResource = new ClassPathResource("/sample-pipeline.yml");
        String pipelineYaml = copyResourceToString(pipelineYamlResource);

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = Map.of(
            "projectName", "Some Project Name!",
            "pipelineYaml", pipelineYaml,
            "dryRun", "true"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);

        String pipelineName = ConcoursePipelineUtil.derivePipelineName("Some Project Name!");
        assertThat(taskResult.getOutputData()).containsEntry("pipelineName", pipelineName);

        String pipelineUrl = String.format("%s/teams/%s/pipelines/%s",
            properties.getApiHost(), properties.getTeamName(), pipelineName);
        assertThat(taskResult.getOutputData()).containsEntry("pipelineUrl", pipelineUrl);
    }

    private String copyResourceToString(Resource resource) {
        String pipelineYaml;
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            pipelineYaml = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pipelineYaml;
    }

}
