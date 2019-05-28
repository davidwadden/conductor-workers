package io.pivotal.conductor.worker.tracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(classes = {
    TrackerTokenAuthenticationInterceptorTest.ContextConfiguration.class,
})
@ExtendWith(SpringExtension.class)
class TrackerTokenAuthenticationInterceptorTest {

    @Autowired
    private TrackerProperties properties;
    @Autowired
    private CreateTrackerProjectWorker worker;
    @Autowired
    private RestOperations restOperations;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer((RestTemplate) restOperations);
    }

    @Test
    void interceptorTest() {
        mockServer
            .expect(requestTo(startsWith("https://www.pivotaltracker.com")))
            .andExpect(header("X-TrackerToken", "some-api-key"))
            .andRespond(withSuccess());

        String requestUrl = "https://www.pivotaltracker.com/some-api-endpoint";

        RequestEntity<Void> requestEntity = RequestEntity
            .get(URI.create(requestUrl))
            .build();

        restOperations.exchange(requestEntity, Void.class);

        mockServer.verify();
    }

    @Test
    void workerTest() throws IOException, URISyntaxException {
        Path postResponsePath =
            Paths.get(this.getClass().getResource("/tracker/post-projects-response.json").toURI());
        String responseBody = new String(Files.readAllBytes(postResponsePath));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("X-Tracker-Project-Version", Collections.singletonList("1"));

        mockServer
            .expect(requestTo("https://www.pivotaltracker.com/services/v5/projects"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-TrackerToken", "some-api-key"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(
                withSuccess(responseBody, MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
            );

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = Map.of("projectName", "some-project-name");
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("projectUrl", "https://www.pivotaltracker.com/n/projects/678");
    }

    @Import(TrackerWorkerConfig.class)
    @Configuration
    public static class ContextConfiguration {

        @Bean
        public TrackerProperties trackerProperties() {
            TrackerProperties trackerProperties = new TrackerProperties();

            trackerProperties.setApiKey("some-api-key");
            trackerProperties.setAccountId(978);

            return trackerProperties;
        }
    }
}
