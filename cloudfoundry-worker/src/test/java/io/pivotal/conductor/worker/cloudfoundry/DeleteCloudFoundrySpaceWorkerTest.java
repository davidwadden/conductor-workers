package io.pivotal.conductor.worker.cloudfoundry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
class DeleteCloudFoundrySpaceWorkerTest {

    @Mock
    private CloudFoundrySpaceClient mockCloudFoundrySpaceClient;
    private DeleteCloudFoundrySpaceWorker worker;

    @BeforeEach
    void setUp() {
        worker = new DeleteCloudFoundrySpaceWorker(mockCloudFoundrySpaceClient);
    }

    @Test
    void execute() {
        doReturn(true)
            .when(mockCloudFoundrySpaceClient)
            .deleteSpace(any(), any(), any());

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of(
            "foundationName", "some-foundation-name",
            "organizationName", "some-organization-name",
            "spaceName", "some-space-name"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockCloudFoundrySpaceClient)
            .deleteSpace("some-foundation-name", "some-organization-name", "some-space-name");

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", true);
    }

    @Test
    void dryRun() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = ImmutableMap.of(
            "foundationName", "some-foundation-name",
            "organizationName", "some-organization-name",
            "spaceName", "some-space-name",
            "dryRun", "true"
        );
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verifyZeroInteractions(mockCloudFoundrySpaceClient);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("wasDeleted", false);
    }
}
