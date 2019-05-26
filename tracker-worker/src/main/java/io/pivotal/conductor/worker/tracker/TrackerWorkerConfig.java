package io.pivotal.conductor.worker.tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(TrackerConfig.class)
@Configuration
public class TrackerWorkerConfig {

    @Autowired
    private TrackerProperties properties;

}
