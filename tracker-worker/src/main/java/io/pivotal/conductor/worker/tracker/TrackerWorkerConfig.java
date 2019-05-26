package io.pivotal.conductor.worker.tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestOperations;

@Import(TrackerConfig.class)
@Configuration
public class TrackerWorkerConfig {

    @Autowired
    private TrackerProperties properties;

    @Bean
    public CreateTrackerProjectWorker createTrackerProjectWorker(
        RestOperations trackerRestOperations) {
        return new CreateTrackerProjectWorker(properties, trackerRestOperations);
    }

}
