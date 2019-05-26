package io.pivotal.conductor.worker.jira;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestOperations;

@Import(JiraConfig.class)
@Configuration
public class JiraWorkerConfig {

    @Autowired
    private JiraProperties properties;

    @Bean
    public CreateJiraProjectWorker createJiraProjectWorker(RestOperations jiraRestOperations,
        ProjectKeyGenerator projectKeyGenerator) {
        return new CreateJiraProjectWorker(properties, jiraRestOperations, projectKeyGenerator);
    }

    @Bean
    public DeleteJiraProjectWorker deleteJiraProjectWorker(RestOperations jiraRestOperations) {
        return new DeleteJiraProjectWorker(properties, jiraRestOperations);
    }

}