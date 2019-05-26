package io.pivotal.conductor.worker.cloudfoundry;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateMysqlDatabaseServiceWorkerTest {

    private CreateMysqlDatabaseServiceWorker worker;

    @BeforeEach
    void setUp() {
        worker = new CreateMysqlDatabaseServiceWorker();
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

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("databaseName", "some-project-name-database-some-suffix");
    }
}
