package io.pivotal.conductor.worker.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class DeleteGitHubRepositoryWorkerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private GitHubProperties properties;
    private MockRestServiceServer mockServer;

    private DeleteGitHubRepositoryWorker worker;

    @BeforeEach
    void setUp() {
        properties = new GitHubProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        worker = new DeleteGitHubRepositoryWorker(properties, restTemplate);
    }

    @Test
    void execute() {
        properties.setToken("some-api-key");
        properties.setOrganizationName("some-org-name");

        mockServer
            .expect(requestTo("https://api.github.com/repos/some-org-name/some-repository-name"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "token some-api-key"))
            .andExpect(header(HttpHeaders.ACCEPT, "application/vnd.github.v3+json"))
            .andRespond(withNoContent());

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Repository Name!");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", true);
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
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", false);
    }

}
