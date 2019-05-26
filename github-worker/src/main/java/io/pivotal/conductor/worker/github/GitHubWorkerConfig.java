package io.pivotal.conductor.worker.github;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestOperations;

@Import(GitHubConfig.class)
@Configuration
public class GitHubWorkerConfig {

    @Autowired
    private GitHubProperties properties;

    @Bean
    public CreateGitHubRepositoryWorker createGitHubRepositoryWorker(
        RestOperations gitHubRestOperations) {
        return new CreateGitHubRepositoryWorker(properties, gitHubRestOperations);
    }

    @Bean
    public DeleteGitHubRepositoryWorker deleteGitHubRepositoryWorker(
        RestOperations gitHubRestOperations) {
        return new DeleteGitHubRepositoryWorker(properties, gitHubRestOperations);
    }

}
