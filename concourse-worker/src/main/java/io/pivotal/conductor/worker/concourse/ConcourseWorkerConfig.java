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
    public DeleteConcoursePipelineWorker deleteConcoursePipelineWorker(
        RestOperations concourseRestOperations) {
        return new DeleteConcoursePipelineWorker(properties, concourseRestOperations);
    }

}
