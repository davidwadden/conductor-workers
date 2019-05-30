package io.pivotal.conductor.worker.concourse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class ExposeConcoursePipelineWorkerTest {

    private ConcourseProperties properties;
    private MockRestServiceServer mockServer;
    private ExposeConcoursePipelineWorker worker;

    @BeforeEach
    void setUp() {
        properties = new ConcourseProperties();
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        worker = new ExposeConcoursePipelineWorker(properties, restTemplate);
    }

    @Test
    void execute() {
        properties.setApiHost("https://some-api-host");
        properties.setTeamName("some-team-name");
        properties.setUsername("some-username");
        properties.setPassword("some-password");

        String requestUrl =
            String.format("%s/api/v1/teams/%s/pipelines/some-project-name/expose",
                properties.getApiHost(), properties.getTeamName());
        mockServer
            .expect(requestTo(requestUrl))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withSuccess());

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = Map.of("projectName", "Some Project Name!");
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
    }

    @Test
    void dryRun() {
        properties.setApiHost("https://some-api-host");
        properties.setTeamName("some-team-name");
        properties.setUsername("some-username");
        properties.setPassword("some-password");

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
    }
}
