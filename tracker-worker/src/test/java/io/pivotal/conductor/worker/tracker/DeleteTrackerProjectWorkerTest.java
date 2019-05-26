package io.pivotal.conductor.worker.tracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class DeleteTrackerProjectWorkerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TrackerProperties properties;
    private MockRestServiceServer mockServer;

    private DeleteTrackerProjectWorker worker;

    @BeforeEach
    void setUp() {
        properties = new TrackerProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        worker = new DeleteTrackerProjectWorker(properties, restTemplate);
    }

    @Test
    void execute() throws URISyntaxException, IOException {
        properties.setApiKey("some-api-key");
        properties.setAccountId(90);

        Path getProjectsResponsePath =
            Paths.get(this.getClass().getResource("/tracker/get-projects-response.json").toURI());
        String getProjectsResponseBody = new String(Files.readAllBytes(getProjectsResponsePath));

        mockServer
            .expect(requestTo("https://www.pivotaltracker.com/services/v5/projects?account_ids=90"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("X-TrackerToken", "some-api-key"))
            .andRespond(withSuccess(getProjectsResponseBody, MediaType.APPLICATION_JSON));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("X-Tracker-Project-Version", Collections.singletonList("44"));

        mockServer
            .expect(requestTo("https://www.pivotaltracker.com/services/v5/projects/100"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header("X-TrackerToken", "some-api-key"))
            .andRespond(
                withNoContent()
                    .headers(httpHeaders)
            );

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);

        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", true);
    }

    @Test
    void execute_notFound() throws URISyntaxException, IOException {
        properties.setApiKey("some-api-key");
        properties.setAccountId(90);

        Path getProjectsResponsePath =
            Paths.get(this.getClass().getResource("/tracker/get-projects-response.json").toURI());
        String getProjectsResponseBody = new String(Files.readAllBytes(getProjectsResponsePath));

        mockServer
            .expect(anything())
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("X-TrackerToken", "some-api-key"))
            .andRespond(withSuccess(getProjectsResponseBody, MediaType.APPLICATION_JSON));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);

        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Project That Does Not Exist");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", false);
    }

    @Test
    void dryRun() {
        properties.setApiKey("some-api-key");
        properties.setAccountId(90);

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);

        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name");
            put("dryRun", "true");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", false);
    }

}
