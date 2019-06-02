package io.pivotal.conductor.worker.template;

import io.pivotal.conductor.lib.template.FreemarkerTemplateProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateConfig {

    @Autowired
    private TemplateProperties properties;

    @Bean
    public freemarker.template.Configuration freemarkerConfiguration() {
        return new freemarker.template.Configuration(
            freemarker.template.Configuration.VERSION_2_3_28);
    }

    @Bean
    public FreemarkerTemplateProcessor freemarkerTemplateProcessor(
        freemarker.template.Configuration freemarkerConfiguration) {
        return new FreemarkerTemplateProcessor(freemarkerConfiguration);
    }

}
