package io.pivotal.conductor.worker.cloudfoundry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.google.common.collect.ImmutableMap;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryProperties.CloudFoundryFoundationProperties;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateRabbitMqServiceWorkerTest {

    private CloudFoundryProperties properties;
    @Mock
    private CloudFoundryServiceClient mockCloudFoundryServiceClient;
    private CreateRabbitMqServiceWorker worker;

    @BeforeEach
    void setUp() {
        properties = new CloudFoundryProperties();
        worker = new CreateRabbitMqServiceWorker(properties, mockCloudFoundryServiceClient);
    }

    @Test
    void execute() {
        CloudFoundryFoundationProperties foundationProperties = new CloudFoundryFoundationProperties();
        foundationProperties.setOrganization("some-organization-name");
        properties.getFoundations().put("some-foundation-name", foundationProperties);

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of(
            "foundationName", "some-foundation-name",
            "projectName", "Some Project Name!",
            "spaceNameSuffix", "some-suffix",
            "spaceName", "some-space-name"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockCloudFoundryServiceClient)
            .createRabbitMqBroker("some-foundation-name", "some-organization-name",
                "some-space-name", "some-project-name-amqp-some-suffix");

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("serviceInstanceName", "some-project-name-amqp-some-suffix");
    }

    @Test
    void dryRun() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of(
            "foundationName", "some-foundation-name",
            "projectName", "Some Project Name!",
            "spaceNameSuffix", "some-suffix",
            "spaceName", "some-space-name",
            "dryRun", "true"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verifyZeroInteractions(mockCloudFoundryServiceClient);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("serviceInstanceName", "some-project-name-amqp-some-suffix");
    }
}
