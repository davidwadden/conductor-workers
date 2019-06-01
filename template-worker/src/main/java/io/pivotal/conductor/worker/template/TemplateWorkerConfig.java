package io.pivotal.conductor.worker.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

@Import(TemplateConfig.class)
@Configuration
public class TemplateWorkerConfig {

    @Autowired
    private TemplateProperties properties;

    @Bean
    public InterpolateConcoursePipelineWorker interpolateConcoursePipelineWorker(
        @Value("classpath:/template.yml") Resource templateYamlResource) {
        return new InterpolateConcoursePipelineWorker(properties, templateYamlResource);
    }

}
