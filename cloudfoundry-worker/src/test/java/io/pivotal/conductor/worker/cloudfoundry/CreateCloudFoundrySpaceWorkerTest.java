package io.pivotal.conductor.worker.cloudfoundry;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateCloudFoundrySpaceWorkerTest {

    private CreateCloudFoundrySpaceWorker worker;

    @BeforeEach
    void setUp() {
        worker = new CreateCloudFoundrySpaceWorker();
    }

    @Test
    void execute() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<String, Object>() {{
            put("projectName", "Some Project Name!");
            put("spaceNameSuffix", "some-suffix");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("spaceName", "some-project-name-some-suffix");
    }

}
