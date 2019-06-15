package io.pivotal.conductor.worker.app;

import com.netflix.conductor.client.task.WorkflowTaskCoordinator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ApplicationTest {

    @MockBean
    private WorkflowTaskCoordinator noOpTaskCoordinator;

    @Test
    void contextLoads() {
    }

}
