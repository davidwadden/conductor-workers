package io.pivotal.conductor.worker.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateConfig {

    @Autowired
    private TemplateProperties properties;

}
