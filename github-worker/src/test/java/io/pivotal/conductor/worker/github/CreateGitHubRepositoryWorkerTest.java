package io.pivotal.conductor.worker.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import io.pivotal.conductor.worker.github.CreateGitHubRepositoryWorker.CreateRepositoryRequestDto;
import io.pivotal.conductor.worker.github.CreateGitHubRepositoryWorker.CreateRepositoryResponseDto;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class CreateGitHubRepositoryWorkerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private GitHubProperties properties;
    private MockRestServiceServer mockServer;

    private CreateGitHubRepositoryWorker worker;

    @BeforeEach
    void setUp() {
        properties = new GitHubProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        worker = new CreateGitHubRepositoryWorker(properties, restTemplate);
    }

    @Test
    void execute() throws JsonProcessingException {
        properties.setToken("some-api-key");
        properties.setOrganizationName("some-org-name");

        CreateRepositoryRequestDto requestDto =
            new CreateRepositoryRequestDto("some-repository-name");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        CreateRepositoryResponseDto responseDto =
            new CreateRepositoryResponseDto("http://github.com/some-org-name/some-repository-name");
        String responseBody = objectMapper.writeValueAsString(responseDto);

        mockServer
            .expect(requestTo("https://api.github.com/orgs/some-org-name/repos"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "token some-api-key"))
            .andExpect(header(HttpHeaders.ACCEPT, "application/vnd.github.v3+json"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(requestBody))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Repository Name!");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("repositoryUrl", "http://github.com/some-org-name/some-repository-name");
    }

    @Test
    void dryRun() {
        properties.setToken("some-api-key");
        properties.setOrganizationName("some-org-name");

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Repository Name!");
            put("dryRun", "true");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("repositoryUrl", "http://github.com/some-org-name/some-repository-name");
    }

}
