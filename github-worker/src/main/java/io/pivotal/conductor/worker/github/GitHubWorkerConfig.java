package io.pivotal.conductor.worker.github;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitHubWorkerConfig {

    @Autowired
    private GitHubProperties properties;

}
