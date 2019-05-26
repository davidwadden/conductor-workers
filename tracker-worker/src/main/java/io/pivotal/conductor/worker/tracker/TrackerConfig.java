package io.pivotal.conductor.worker.tracker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TrackerConfig {

    @Bean
    public RestOperations trackerRestOperations() {
        return new RestTemplate();
    }

}
