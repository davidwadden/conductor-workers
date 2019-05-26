package io.pivotal.conductor.worker.jira;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JiraConfig {

    @Bean
    public RestOperations jiraRestOperations() {
        return new RestTemplate();
    }

    @Bean
    public ProjectKeyGenerator projectKeyGenerator() {
        return new RandomProjectKeyGenerator();
    }
}
