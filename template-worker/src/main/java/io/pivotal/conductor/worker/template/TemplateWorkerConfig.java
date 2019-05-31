package io.pivotal.conductor.worker.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(TemplateConfig.class)
@Configuration
public class TemplateWorkerConfig {

    @Autowired
    private TemplateProperties properties;

}
