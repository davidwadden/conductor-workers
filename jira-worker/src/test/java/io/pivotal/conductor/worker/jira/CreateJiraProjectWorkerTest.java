package io.pivotal.conductor.worker.jira;

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
class CreateJiraProjectWorkerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProjectKeyGenerator mockProjectKeyGenerator;
    private JiraProperties properties;
    private MockRestServiceServer mockServer;

    private CreateJiraProjectWorker worker;

    @BeforeEach
    void setUp() {
        properties = new JiraProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        worker = new CreateJiraProjectWorker(properties, restTemplate, mockProjectKeyGenerator);
    }

    @Test
    void execute() throws JsonProcessingException {
        doReturn("PROJECT-KEY")
            .when(mockProjectKeyGenerator)
            .generateKey();

        properties.setApiUrl("https://some-api-url");
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setAccountId("some-account-id");

        Map<String, Object> requestDto = Map.of(
            "key", "PROJECT-KEY",
            "name", "Some Project Name!",
            "projectTypeKey", "software",
            "projectTemplateKey", "com.pyxis.greenhopper.jira:gh-scrum-template",
            "leadAccountId", "some-account-id"
        );
        String requestBody = objectMapper.writeValueAsString(requestDto);

        mockServer
            .expect(requestTo(String.format("%s/rest/api/3/project", properties.getApiUrl())))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(requestBody))
            .andRespond(withStatus(HttpStatus.CREATED));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = Map.of("projectName", "Some Project Name!");
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        verify(mockProjectKeyGenerator).generateKey();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("projectUrl", "https://some-api-url/projects/PROJECT-KEY");
    }

    @Test
    void dryRun() {
        doReturn("PROJECT-KEY").when(mockProjectKeyGenerator).generateKey();

        properties.setApiUrl("https://some-api-url");
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setAccountId("some-account-id");

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = Map.of(
            "projectName", "Some Project Name!",
            "dryRun", "true"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("projectUrl", "https://some-api-url/projects/PROJECT-KEY");
    }

}
