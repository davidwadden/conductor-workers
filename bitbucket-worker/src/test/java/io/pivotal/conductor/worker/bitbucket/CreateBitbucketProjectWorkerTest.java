package io.pivotal.conductor.worker.bitbucket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class CreateBitbucketProjectWorkerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProjectKeyGenerator mockProjectKeyGenerator;
    private BitbucketProperties properties;
    private MockRestServiceServer mockServer;

    private CreateBitbucketProjectWorker worker;

    @BeforeEach
    void setUp() {
        properties = new BitbucketProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        worker =
            new CreateBitbucketProjectWorker(properties, restTemplate, mockProjectKeyGenerator);
    }

    @Test
    void execute() throws JsonProcessingException {
        doReturn("PROJECT-KEY")
            .when(mockProjectKeyGenerator)
            .generateKey();

        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setTeamName("some-team-name");

        Map<String, Object> responseDto = new HashMap<>() {{
            put("name", "Some Project Name!");
            put("key", "PROJECT-KEY");
            put("is_private", false);
        }};
        String responseBody = objectMapper.writeValueAsString(responseDto);

        mockServer
            .expect(requestTo("https://api.bitbucket.org/2.0/teams/some-team-name/projects/"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(responseBody))
            .andRespond(withStatus(HttpStatus.CREATED));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name!");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        verify(mockProjectKeyGenerator).generateKey();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("projectKey", "PROJECT-KEY");
    }

    @Test
    void dryRun() {
        doReturn("PROJECT-KEY")
            .when(mockProjectKeyGenerator)
            .generateKey();

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

        verify(mockProjectKeyGenerator).generateKey();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("projectKey", "PROJECT-KEY");
    }
}
