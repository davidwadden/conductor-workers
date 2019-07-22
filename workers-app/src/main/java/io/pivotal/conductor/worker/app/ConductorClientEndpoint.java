package io.pivotal.conductor.worker.app;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

@Endpoint(id = "conductor")
@Component
public class ConductorClientEndpoint {

    private final ConductorLifecycle conductorLifecycle;

    public ConductorClientEndpoint(ConductorLifecycle conductorLifecycle) {
        this.conductorLifecycle = conductorLifecycle;
    }

    @ReadOperation
    public Boolean isRunning() {
        return conductorLifecycle.isRunning();
    }

    @WriteOperation
    public void setRunningStatus(Boolean isRunningStatus) {
        if (!isRunningStatus) {
            conductorLifecycle.stop();
        } else {
            conductorLifecycle.start();
        }
    }

    @WriteOperation
    public void stop() {
        conductorLifecycle.stop();
    }
}
