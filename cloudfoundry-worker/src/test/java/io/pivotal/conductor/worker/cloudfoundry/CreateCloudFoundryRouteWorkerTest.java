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
        properties.setDomain("some-domain");

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name!");
            put("hostnameSuffix", "-some-suffix");
            put("spaceName", "some-space-name");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockCloudFoundryRouteClient).createRoute("some-space-name", "some-project-name-some-suffix", "some-domain");

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("hostname", "some-project-name-some-suffix");
    }

    @Test
    void dryRun() {
        properties.setDomain("some-domain");

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Project Name!");
            put("hostnameSuffix", "-some-suffix");
            put("spaceName", "some-space-name");
            put("dryRun", "true");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verifyZeroInteractions(mockCloudFoundryRouteClient);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("hostname", "some-project-name-some-suffix");
    }
}
