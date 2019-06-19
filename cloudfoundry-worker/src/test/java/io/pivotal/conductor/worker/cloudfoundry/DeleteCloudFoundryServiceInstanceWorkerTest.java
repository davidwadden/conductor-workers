package io.pivotal.conductor.worker.cloudfoundry;

import static org.assertj.core.api.Assertions.assertThat;
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
class DeleteCloudFoundryServiceInstanceWorkerTest {

    @Mock
    private CloudFoundryServiceClient mockCloudFoundryServiceClient;
    private DeleteCloudFoundryServiceInstanceWorker worker;

    @BeforeEach
    void setUp() {
        worker = new DeleteCloudFoundryServiceInstanceWorker(mockCloudFoundryServiceClient);
    }

    @Test
    void execute() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<String, Object>() {{
            put("foundationName", "some-foundation-name");
            put("organizationName", "some-organization-name");
            put("spaceName", "some-space-name");
            put("serviceInstanceName", "some-service-instance-name");
        }};

        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockCloudFoundryServiceClient)
            .deleteServiceInstance("some-foundation-name", "some-organization-name",
                "some-space-name", "some-service-instance-name");

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
    }

    @Test
    void dryRun() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<String, Object>() {{
            put("foundationName", "some-foundation-name");
            put("organizationName", "some-organization-name");
            put("spaceName", "some-space-name");
            put("serviceInstanceName", "some-service-instance-name");
            put("dryRun", "true");
        }};

        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verifyZeroInteractions(mockCloudFoundryServiceClient);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
    }
}
