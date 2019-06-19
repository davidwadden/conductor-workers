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
class CreateCloudFoundryServiceInstanceWorkerTest {

    @Mock
    private CloudFoundryServiceClient mockCloudFoundryServiceClient;
    private CreateCloudFoundryServiceInstanceWorker worker;

    @BeforeEach
    void setUp() {
        worker = new CreateCloudFoundryServiceInstanceWorker(mockCloudFoundryServiceClient);
    }

    @Test
    void execute() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<String, Object>() {{
            put("foundationName", "some-foundation-name");
            put("organizationName", "some-organization-name");
            put("spaceName", "some-space-name");
            put("serviceName", "some-service-name");
            put("servicePlanName", "some-service-plan-name");
            put("serviceInstanceName", "some-service-instance-name");
        }};

        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockCloudFoundryServiceClient)
            .createServiceInstance("some-foundation-name", "some-organization-name",
                "some-space-name", "some-service-name", "some-service-plan-name",
                "some-service-instance-name");

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
            put("serviceName", "some-service-name");
            put("servicePlanName", "some-service-plan-name");
            put("serviceInstanceName", "some-service-instance-name");
            put("dryRun", "true");
        }};

        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verifyZeroInteractions(mockCloudFoundryServiceClient);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
    }
}
