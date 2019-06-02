package io.pivotal.conductor.worker.concourse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestOperations;

@Import(ConcourseConfig.class)
@Configuration
public class ConcourseWorkerConfig {

    @Autowired
    private ConcourseProperties properties;

    @Bean
    public UpdateConcoursePipelineWorker updateConcoursePipelineWorker(
        RestOperations concourseOAuth2RestOperations) {
        return new UpdateConcoursePipelineWorker(properties, concourseOAuth2RestOperations
        );
    }

    @Bean
    public DeleteConcoursePipelineWorker deleteConcoursePipelineWorker(
        RestOperations concourseOAuth2RestOperations) {
        return new DeleteConcoursePipelineWorker(properties, concourseOAuth2RestOperations);
    }

    @Bean
    public ExposeConcoursePipelineWorker exposeConcoursePipelineWorker(
        RestOperations concourseOAuth2RestOperations) {
        return new ExposeConcoursePipelineWorker(properties, concourseOAuth2RestOperations);
    }

    @Bean
    public UnpauseConcoursePipelineWorker unpauseConcoursePipelineWorker(
        RestOperations concourseOAuth2RestOperations) {
        return new UnpauseConcoursePipelineWorker(properties, concourseOAuth2RestOperations);
    }

}
