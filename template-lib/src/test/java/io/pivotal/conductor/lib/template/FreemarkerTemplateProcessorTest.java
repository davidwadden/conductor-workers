package io.pivotal.conductor.lib.template;

import static org.assertj.core.api.Assertions.assertThat;

import freemarker.template.Configuration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

class FreemarkerTemplateProcessorTest {

    static Semaphore semaphore = new Semaphore(1);

    private static final Logger logger =
        LoggerFactory.getLogger(FreemarkerTemplateProcessorTest.class);

    private FreemarkerTemplateProcessor freemarkerTemplateProcessor;
    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = new Configuration(Configuration.VERSION_2_3_28);
        freemarkerTemplateProcessor = new FreemarkerTemplateProcessor(configuration);
    }

    @Test
    void process() throws IOException {
        byte[] templateBytes = Files.readAllBytes(
            Paths.get(new ClassPathResource("/fixtures/sample-template.txt.ftl").getURI()));
        InputStream inputStream = new ByteArrayInputStream(templateBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("someVariable", "someValue");
        dataModel.put("enableSection", true);

        freemarkerTemplateProcessor.process(inputStream, outputStream, dataModel);

        String processedText = outputStream.toString(Charset.defaultCharset().name());

        assertThat(processedText)
            .contains("Shared file contents")
            .contains("Interpolate someValue")
            .contains("The section is enabled");
    }

    @Test
    void process_disableSection() throws IOException {
        byte[] templateBytes = Files.readAllBytes(
            Paths.get(new ClassPathResource("/fixtures/sample-template.txt.ftl").getURI()));
        InputStream inputStream = new ByteArrayInputStream(templateBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("someVariable", "someValue");
        dataModel.put("enableSection", false);

        freemarkerTemplateProcessor.process(inputStream, outputStream, dataModel);

        String processedText = outputStream.toString(Charset.defaultCharset().name());

        assertThat(processedText)
            .contains("Shared file contents")
            .contains("Interpolate someValue");
    }

}
