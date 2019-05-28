package io.pivotal.conductor.worker.concourse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

class DeleteConcoursePipelineWorkerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ConcourseProperties properties;
    private MockRestServiceServer mockServer;
    private DeleteConcoursePipelineWorker worker;

    @BeforeEach
    void setUp() {
        properties = new ConcourseProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        worker = new DeleteConcoursePipelineWorker(properties, restTemplate);
    }

    @Test
    void execute() throws JsonProcessingException {
        properties.setApiHost("https://some-api-host");
        properties.setTeamName("some-team-name");
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setShouldExposePipeline("false");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>() {{
            add("grant_type", "password");
            add("username", "some-username");
            add("password", "some-password");
            add("scope", "openid profile email federated:id groups");
        }};

        Map<String, Object> responseDto = new HashMap<>() {{
            put("access_token", "some-access-token");
            put("token_type", "Bearer");
            put("expiry", "2019-05-29T00:11:26Z");
        }};
        String responseBody = objectMapper.writeValueAsString(responseDto);

        String authToken =
            Base64.getEncoder().encodeToString(("fly" + ":" + "Zmx5").getBytes());
        mockServer
            .expect(requestTo("https://some-api-host/sky/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", authToken)))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(content().formData(formData))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        String requestUrl =
            String.format("%s/api/v1/teams/%s/pipelines/some-project-name",
                properties.getApiHost(), properties.getTeamName());
        mockServer
            .expect(requestTo(requestUrl))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer some-access-token"))
            .andRespond(withNoContent());

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
    void dryRun() {
        properties.setApiHost("https://some-api-host");
        properties.setTeamName("some-team-name");
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setShouldExposePipeline("false");

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
