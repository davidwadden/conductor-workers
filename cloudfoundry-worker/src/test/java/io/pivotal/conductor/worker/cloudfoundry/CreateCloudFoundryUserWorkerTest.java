package io.pivotal.conductor.worker.cloudfoundry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.google.common.collect.ImmutableMap;
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
class CreateCloudFoundryUserWorkerTest {

    @Mock
    private CloudFoundryUserClient mockCloudFoundryUserClient;
    private CreateCloudFoundryUserWorker worker;

    @BeforeEach
    void setUp() {
        worker = new CreateCloudFoundryUserWorker(mockCloudFoundryUserClient);
    }

    @Test
    void execute() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<String, Object>() {{
            put("foundationName", "some-foundation-name");
            put("userName", "some-user-name");
            put("password", "some-password");
            put("origin", "some-origin");
            put("externalId", "some-external-id");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockCloudFoundryUserClient)
            .createUser("some-foundation-name", "some-user-name", "some-password",
                "some-origin", "some-external-id");

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
    }

    @Test
    void dryRun() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);

        Map<String, Object> inputData = new HashMap<String, Object>() {{
            put("foundationName", "some-foundation-name");
            put("userName", "some-user-name");
            put("password", "some-password");
            put("origin", "some-origin");
            put("externalId", "some-external-id");
            put("dryRun", "true");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verifyZeroInteractions(mockCloudFoundryUserClient);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
    }

}
