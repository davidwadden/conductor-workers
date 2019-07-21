package io.pivotal.conductor.worker.app;

import com.netflix.conductor.client.task.WorkflowTaskCoordinator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConductorLifecycle implements SmartLifecycle {

    private final WorkflowTaskCoordinator taskCoordinator;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public ConductorLifecycle(WorkflowTaskCoordinator taskCoordinator) {
        this.taskCoordinator = taskCoordinator;
    }

    @Override
    public void start() {
        taskCoordinator.init();
        isRunning.set(true);
    }

    @Override
    public void stop() {
        taskCoordinator.shutdown();
        isRunning.set(false);
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }
}
