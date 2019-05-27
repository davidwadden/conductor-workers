package io.pivotal.conductor.worker.jira;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JiraConfig {

    @Autowired
    private JiraProperties properties;

    @Bean
    public RestOperations jiraRestOperations() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(jiraBasicAuthenticationInterceptor()));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestInterceptor jiraBasicAuthenticationInterceptor() {
        return new BasicAuthenticationInterceptor(
            properties.getUsername(), properties.getPassword());
    }

    @Bean
    public ProjectKeyGenerator jiraProjectKeyGenerator() {
        return new RandomProjectKeyGenerator();
    }
}
