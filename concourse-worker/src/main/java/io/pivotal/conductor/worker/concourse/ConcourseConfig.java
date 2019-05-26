package io.pivotal.conductor.worker.concourse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConcourseConfig {

    @Bean
    public RestOperations concourseRestOperations() {
        return new RestTemplate();
    }

}
