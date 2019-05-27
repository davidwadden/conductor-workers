package io.pivotal.conductor.worker.bitbucket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class DeleteBitbucketProjectWorkerTest {

    private static final String LIST_PROJECTS_RESPONSE_BODY = "{\n"
        + "    \"values\": [\n"
        + "        {\n"
        + "            \"name\": \"First Project Name\",\n"
        + "            \"key\": \"FIRST\"\n"
        + "        },\n"
        + "        {\n"
        + "            \"name\": \"Some Project Name!\",\n"
        + "            \"key\": \"RAND\"\n"
        + "        }\n"
        + "    ]\n"
        + "}";

    private static final String EMPTY_LIST_PROJECTS_RESPONSE_BODY = "{\n"
        + "    \"values\": []\n"
        + "}";

    private BitbucketProperties properties;
    private MockRestServiceServer mockServer;

    private DeleteBitbucketProjectWorker worker;

    @BeforeEach
    void setUp() {
        properties = new BitbucketProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        worker = new DeleteBitbucketProjectWorker(properties, restTemplate);
    }

    @Test
    void execute() {
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setTeamName("some-team-name");

        String usernamePassword =
            String.format("%s:%s", properties.getUsername(), properties.getPassword());
        String authToken = Base64.getEncoder().encodeToString(usernamePassword.getBytes());

        mockServer
            .expect(requestTo("https://api.bitbucket.org/2.0/teams/some-team-name/projects/?pagelen=100"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", authToken)))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(LIST_PROJECTS_RESPONSE_BODY)
            );

        mockServer
            .expect(requestTo("https://api.bitbucket.org/2.0/teams/some-team-name/projects/RAND"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", authToken)))
            .andRespond(withStatus(HttpStatus.NO_CONTENT));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name!");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", true);
    }

    @Test
    void execute_emptyList() {
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setTeamName("some-team-name");

        String usernamePassword =
            String.format("%s:%s", properties.getUsername(), properties.getPassword());
        String authToken = Base64.getEncoder().encodeToString(usernamePassword.getBytes());

        mockServer
            .expect(requestTo("https://api.bitbucket.org/2.0/teams/some-team-name/projects/?pagelen=100"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", authToken)))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(EMPTY_LIST_PROJECTS_RESPONSE_BODY)
            );

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name!");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", false);
    }

    @Test
    void dryRun() {
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setTeamName("some-team-name");

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name!");
            put("dryRun", "true");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", false);
    }
}
