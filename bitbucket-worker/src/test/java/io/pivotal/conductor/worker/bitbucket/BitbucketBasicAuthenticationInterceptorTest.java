package io.pivotal.conductor.worker.bitbucket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.net.URI;
import java.util.Base64;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(classes = {
    BitbucketBasicAuthenticationInterceptorTest.ContextConfiguration.class,
})
@ExtendWith(SpringExtension.class)
class BitbucketBasicAuthenticationInterceptorTest {

    @Autowired
    private BitbucketProperties properties;
    @Autowired
    private CreateBitbucketProjectWorker worker;
    @Autowired
    private RestOperations restOperations;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer((RestTemplate) restOperations);
    }

    @Test
    void interceptorTest() {
        String usernamePassword = String.format("%s:%s", properties.getUsername(), properties.getPassword());
        String expectedToken = Base64.getEncoder().encodeToString(usernamePassword.getBytes());

        mockServer
            .expect(requestTo(startsWith("https://api.bitbucket.org/")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", expectedToken)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(withStatus(HttpStatus.CREATED));

        String requestUrl = "https://api.bitbucket.org/2.0/teams/some-team-name/projects/";

        RequestEntity<Void> requestEntity = RequestEntity
            .post(URI.create(requestUrl))
            .contentType(MediaType.APPLICATION_JSON)
            .build();

        restOperations.exchange(requestEntity, Void.class);

        mockServer.verify();
    }

    @Test
    void workerTest() {
        String usernamePassword = String.format("%s:%s", properties.getUsername(), properties.getPassword());
        String expectedToken = Base64.getEncoder().encodeToString(usernamePassword.getBytes());

        mockServer
            .expect(requestTo(startsWith("https://api.bitbucket.org/")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", expectedToken)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(withStatus(HttpStatus.CREATED));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = Map.of("projectName", "some-project-name");
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
    }

    @Import(BitbucketWorkerConfig.class)
    @Configuration
    public static class ContextConfiguration {

        @Bean
        public BitbucketProperties bitbucketProperties() {
            BitbucketProperties bitbucketProperties = new BitbucketProperties();

            bitbucketProperties.setUsername("some-username");
            bitbucketProperties.setPassword("some-password");
            bitbucketProperties.setTeamName("some-team-name");

            return bitbucketProperties;
        }
    }
}
