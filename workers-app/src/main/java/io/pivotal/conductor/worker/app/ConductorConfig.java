package io.pivotal.conductor.worker.app;

import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.task.WorkflowTaskCoordinator;
import com.netflix.conductor.client.task.WorkflowTaskCoordinator.Builder;
import com.netflix.conductor.client.worker.Worker;
import io.pivotal.conductor.worker.bitbucket.BitbucketProperties;
import io.pivotal.conductor.worker.bitbucket.BitbucketWorkerConfig;
import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryProperties;
import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryWorkerConfig;
import io.pivotal.conductor.worker.concourse.ConcourseProperties;
import io.pivotal.conductor.worker.concourse.ConcourseWorkerConfig;
import io.pivotal.conductor.worker.github.GitHubProperties;
import io.pivotal.conductor.worker.github.GitHubWorkerConfig;
import io.pivotal.conductor.worker.jira.JiraProperties;
import io.pivotal.conductor.worker.jira.JiraWorkerConfig;
import io.pivotal.conductor.worker.template.TemplateProperties;
import io.pivotal.conductor.worker.template.TemplateWorkerConfig;
import io.pivotal.conductor.worker.tracker.TrackerProperties;
import io.pivotal.conductor.worker.tracker.TrackerWorkerConfig;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableConfigurationProperties(ConductorProperties.class)
@Configuration
public class ConductorConfig {

    @Autowired
    private ConductorProperties properties;

    @Import({
        BitbucketWorkerConfig.class,
        CloudFoundryWorkerConfig.class,
        ConcourseWorkerConfig.class,
        GitHubWorkerConfig.class,
        JiraWorkerConfig.class,
        TemplateWorkerConfig.class,
        TrackerWorkerConfig.class,
    })
    @Configuration
    static class ImportConfig {

    }

    @Bean
    @ConfigurationProperties("portal.bitbucket")
    public BitbucketProperties bitbucketProperties() {
        return new BitbucketProperties();
    }

    @Bean
    @ConfigurationProperties("portal.cloudfoundry")
    public CloudFoundryProperties cloudFoundryProperties() {
        return new CloudFoundryProperties();
    }

    @Bean
    @ConfigurationProperties("portal.concourse")
    public ConcourseProperties concourseProperties() {
        return new ConcourseProperties();
    }

    @Bean
    @ConfigurationProperties("portal.github")
    public GitHubProperties gitHubProperties() {
        return new GitHubProperties();
    }

    @Bean
    @ConfigurationProperties("portal.jira")
    public JiraProperties jiraProperties() {
        return new JiraProperties();
    }

    @Bean
    @ConfigurationProperties("portal.template")
    public TemplateProperties  templateProperties() {
        return new TemplateProperties();
    }

    @Bean
    @ConfigurationProperties("portal.tracker")
    public TrackerProperties  trackerProperties() {
        return new TrackerProperties();
    }

    @Bean
    public TaskClient taskClient() {
        TaskClient taskClient = new TaskClient();
        taskClient.setRootURI(properties.getConductorRootUri());
        return taskClient;
    }

    @Bean
    public WorkflowTaskCoordinator taskCoordinator(Collection<Worker> workers) {

        WorkflowTaskCoordinator taskCoordinator = new Builder()
            .withTaskClient(taskClient())
            .withWorkers(workers)
            .build();
        taskCoordinator.init();
        return taskCoordinator;
    }

}
