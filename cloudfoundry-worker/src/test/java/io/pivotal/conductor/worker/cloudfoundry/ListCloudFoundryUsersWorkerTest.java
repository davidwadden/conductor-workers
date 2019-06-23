package io.pivotal.conductor.worker.cloudfoundry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.cloudfoundry.uaa.users.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListCloudFoundryUsersWorkerTest {

    @Mock
    private CloudFoundryUserClient mockCloudFoundryUserClient;
    private ListCloudFoundryUsersWorker worker;

    @BeforeEach
    void setUp() {
        worker = new ListCloudFoundryUsersWorker(mockCloudFoundryUserClient);
    }

    @Test
    void execute() {
        doReturn(
            Collections.singletonList(
                UserId.builder()
                    .id("some-id")
                    .origin("some-origin")
                    .userName("some-user-name")
                    .build())
        )
            .when(mockCloudFoundryUserClient)
            .listUsers(any(), any());

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<String, Object>() {{
            put("foundationName", "some-foundation-name");
            put("userName", "some-user-name");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verify(mockCloudFoundryUserClient)
            .listUsers("some-foundation-name", "some-user-name");

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("userCount", 1);
        assertThat(taskResult.getOutputData())
            .containsEntry("userNames", Collections.singletonList("some-user-name"));
    }

    @Test
    void dryRun() {
        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);

        Map<String, Object> inputData = new HashMap<String, Object>() {{
            put("foundationName", "some-foundation-name");
            put("userName", "some-user-name");
            put("dryRun", "true");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        verifyZeroInteractions(mockCloudFoundryUserClient);

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData()).containsEntry("userCount", 0);
        assertThat(taskResult.getOutputData()).hasEntrySatisfying("userNames", entry -> {
            assertThat(entry).isInstanceOf(Collection.class);
            //noinspection unchecked
            Collection<String> entryCollection = (Collection<String>) entry;
            assertThat(entryCollection).isEmpty();
        });
    }

}
