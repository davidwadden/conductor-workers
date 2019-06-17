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
class CreateCloudFoundryRouteWorkerTest {

    @Mock
    private CloudFoundryRouteClient mockCloudFoundryRouteClient;
    private CloudFoundryProperties properties;
    private CreateCloudFoundryRouteWorker worker;

    @BeforeEach
    void setUp() {
        properties = new CloudFoundryProperties();
        worker = new CreateCloudFoundryRouteWorker(properties, mockCloudFoundryRouteClient);
    }

    @Test
    void execute() {
        CloudFoundryFoundationProperties foundationProperties = new CloudFoundryFoundationProperties();
        foundationProperties.setOrganization("some-organization-name");
        foundationProperties.setDomain("some-domain");
        properties.getFoundations()
            .put("some-foundation-name", foundationProperties);

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of(
            "foundationName", "some-foundation-name",
            "projectName", "Some Project Name!",
            "hostnameSuffix", "-some-suffix",
            "spaceName", "some-space-name"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockCloudFoundryRouteClient)
            .createRoute("some-foundation-name", "some-organization-name", "some-space-name",
                "some-project-name-some-suffix", "some-domain");

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("hostname", "some-project-name-some-suffix");
    }

    @Test
    void dryRun() {
        CloudFoundryFoundationProperties foundationProperties =
            new CloudFoundryFoundationProperties();
        foundationProperties.setOrganization("some-organization-name");
        foundationProperties.setDomain("some-domain");
        properties.getFoundations()
            .put("some-foundation-name", foundationProperties);

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of(
            "foundationName", "some-foundation-name",
            "projectName", "Some Project Name!",
            "hostnameSuffix", "-some-suffix",
            "spaceName", "some-space-name",
            "dryRun", "true"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verifyZeroInteractions(mockCloudFoundryRouteClient);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("hostname", "some-project-name-some-suffix");
    }
}
