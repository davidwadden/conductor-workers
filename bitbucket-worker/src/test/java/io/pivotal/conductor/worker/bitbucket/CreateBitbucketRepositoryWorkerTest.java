package io.pivotal.conductor.worker.bitbucket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

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

class CreateBitbucketRepositoryWorkerTest {

    private static final String RESPONSE_BODY = "{\n"
        + "    \"scm\": \"git\",\n"
        + "    \"project\": {\n"
        + "        \"key\": \"PROJECT-KEY\"\n"
        + "    },\n"
        + "    \"is_private\": false\n"
        + "}";

    private BitbucketProperties properties;
    private MockRestServiceServer mockServer;

    private CreateBitbucketRepositoryWorker worker;

    @BeforeEach
    void setUp() {
        properties = new BitbucketProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        worker = new CreateBitbucketRepositoryWorker(properties, restTemplate);
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
            .expect(requestTo(
                "https://api.bitbucket.org/2.0/repositories/some-team-name/some-project-name"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", authToken)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(RESPONSE_BODY))
            .andRespond(withStatus(HttpStatus.CREATED));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name!");
            put("projectKey", "PROJECT-KEY");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("repositoryUrl", "https://bitbucket.org/some-team-name/some-project-name");
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
            put("projectKey", "PROJECT-KEY");
            put("dryRun", "true");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("repositoryUrl", "https://bitbucket.org/some-team-name/some-project-name");
    }

}
