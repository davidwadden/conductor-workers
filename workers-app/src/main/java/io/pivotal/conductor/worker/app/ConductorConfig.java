package io.pivotal.conductor.worker.app;

import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.task.WorkflowTaskCoordinator;
import com.netflix.conductor.client.task.WorkflowTaskCoordinator.Builder;
import com.netflix.conductor.client.worker.Worker;
import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryWorkerConfig;
import java.util.Collection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
public class ConductorConfig {

    @Import(CloudFoundryWorkerConfig.class)
    @Configuration
    static class ImportCloudFoundryWorkerConfig {

    }

    @Bean
    public TaskClient taskClient() {
        TaskClient taskClient = new TaskClient();
        taskClient.setRootURI("http://localhost:8080/api/");
        return taskClient;
    }

    @Bean
    public WorkflowTaskCoordinator taskCoordinator(Collection<Worker> workers) {

        WorkflowTaskCoordinator taskCoordinator = new Builder()
            .withTaskClient(taskClient())
            .withWorkers(workers)
            .build();
        taskCoordinator.init();
        return taskCoordinator;
    }

}
