package io.pivotal.conductor.lib.template;

import freemarker.cache.ByteArrayTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class FreemarkerTemplateProcessor {

    private final Configuration configuration;

    public FreemarkerTemplateProcessor(Configuration configuration) {
        this.configuration = configuration;
    }

    public void process(InputStream inputStream, OutputStream outputStream,
        Map<String, Object> dataModel) {

        byte[] templateBytes;
        try {
            templateBytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteArrayTemplateLoader templateLoader = new ByteArrayTemplateLoader();
        templateLoader.putTemplate("template", templateBytes);
        synchronized (this) {
            try {
                configuration.setTemplateLoader(templateLoader);
                configuration
                    .getTemplate("template")
                    .process(dataModel, new OutputStreamWriter(outputStream));
            } catch (IOException | TemplateException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
