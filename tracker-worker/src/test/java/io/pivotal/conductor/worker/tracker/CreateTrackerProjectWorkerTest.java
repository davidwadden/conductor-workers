package io.pivotal.conductor.worker.tracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import io.pivotal.conductor.worker.tracker.CreateTrackerProjectWorker.CreateTrackerProjectRequestDto;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class CreateTrackerProjectWorkerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TrackerProperties properties;
    private MockRestServiceServer mockServer;

    private CreateTrackerProjectWorker worker;

    @BeforeEach
    void setUp() {
        properties = new TrackerProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        worker = new CreateTrackerProjectWorker(properties, restTemplate);
    }

    @Test
    void execute() throws IOException, URISyntaxException {
        properties.setApiKey("some-api-key");
        properties.setAccountId(90);

        CreateTrackerProjectRequestDto requestDto =
            new CreateTrackerProjectRequestDto("Some Project Name", "public", 90);
        String requestBody = objectMapper.writeValueAsString(requestDto);

        Path postResponsePath =
            Paths.get(this.getClass().getResource("/tracker/post-projects-response.json").toURI());
        String responseBody = new String(Files.readAllBytes(postResponsePath));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("X-Tracker-Project-Version", Collections.singletonList("1"));

        mockServer
            .expect(requestTo("https://www.pivotaltracker.com/services/v5/projects"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(requestBody))
            .andRespond(
                withSuccess(responseBody, MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
            );

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);

        Map<String, Object> inputData = Map.of(
            "projectName", "Some Project Name",
            "stories", Collections.emptyMap()
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("projectUrl", "https://www.pivotaltracker.com/n/projects/678");
    }

    @Test
    void dryRun() {
        properties.setApiKey("some-api-key");
        properties.setAccountId(90);

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);

        Map<String, Object> inputData = Map.of(
            "projectName", "Some Project Name",
            "stories", Collections.emptyMap(),
            "dryRun", "true"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("projectUrl", "https://www.pivotaltracker.com/n/projects/-1");
    }
}
