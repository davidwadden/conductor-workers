package io.pivotal.conductor.worker.cloudfoundry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateRabbitMqServiceWorkerTest {

    @Mock
    private CloudFoundryServiceClient mockCloudFoundryServiceClient;
    private CreateRabbitMqServiceWorker worker;

    @BeforeEach
    void setUp() {
        worker = new CreateRabbitMqServiceWorker(mockCloudFoundryServiceClient);
    }

    @Test
    void execute() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name!");
            put("spaceNameSuffix", "some-suffix");
            put("spaceName", "some-space-name");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockCloudFoundryServiceClient)
            .createRabbitMqBroker("some-project-name-amqp-some-suffix", "some-space-name");

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("serviceInstanceName", "some-project-name-amqp-some-suffix");
    }

    @Test
    void dryRun() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name!");
            put("spaceNameSuffix", "some-suffix");
            put("spaceName", "some-space-name");
            put("dryRun", "true");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verifyZeroInteractions(mockCloudFoundryServiceClient);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("serviceInstanceName", "some-project-name-amqp-some-suffix");
    }
}
