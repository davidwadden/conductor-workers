package io.pivotal.conductor.worker.jira;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.google.common.collect.ImmutableMap;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class DeleteJiraProjectWorkerTest {

    private static final String SEARCH_PROJECTS_RESPONSE_BODY = "{\n"
        + "    \"values\": [\n"
        + "        {\n"
        + "            \"key\": \"IGN1\",\n"
        + "            \"name\": \"Ignore this Project 1\"\n"
        + "        },\n"
        + "        {\n"
        + "            \"key\": \"RAND\",\n"
        + "            \"name\": \"Some Project Name!\"\n"
        + "        },\n"
        + "        {\n"
        + "            \"key\": \"IGN2\",\n"
        + "            \"name\": \"Ignore this Project 2\"\n"
        + "        }\n"
        + "    ]\n"
        + "}";

    private static final String EMPTY_SEARCH_PROJECTS_RESPONSE_BODY = "{\n"
        + "    \"values\": []\n"
        + "}";

    private JiraProperties properties;
    private MockRestServiceServer mockServer;

    private DeleteJiraProjectWorker worker;

    @BeforeEach
    void setUp() {
        properties = new JiraProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        worker = new DeleteJiraProjectWorker(properties, restTemplate);
    }

    @Test
    void execute() {
        properties.setApiUrl("https://some-api-url");
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setAccountId("some-account-id");

        String searchProjectsRequestUrl =
            String.format("%s/rest/api/3/project/search?startAt=0&maxResults=100", properties.getApiUrl());
        mockServer
            .expect(requestTo(searchProjectsRequestUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(SEARCH_PROJECTS_RESPONSE_BODY));

        String deleteProjectRequestUrl =
            String.format("%s/rest/api/3/project/RAND", properties.getApiUrl());
        mockServer
            .expect(requestTo(deleteProjectRequestUrl))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withStatus(HttpStatus.NO_CONTENT));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of("projectName", "Some Project Name!");
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", true);
    }

    @Test
    void execute_emptySearchResults() {
        properties.setApiUrl("https://some-api-url");
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setAccountId("some-account-id");

        mockServer
            .expect(anything())
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(EMPTY_SEARCH_PROJECTS_RESPONSE_BODY));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of("projectName", "Some Project Name!");
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", false);
    }

    @Test
    void dryRun() {
        properties.setApiUrl("https://some-api-url");
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setAccountId("some-account-id");

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of(
            "projectName", "Some Project Name!",
            "dryRun", "true"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", false);
    }
}
