package io.pivotal.conductor.worker.jira;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(classes = {
    JiraBasicAuthenticationInterceptorTest.ContextConfiguration.class,
})
@ExtendWith(SpringExtension.class)
class JiraBasicAuthenticationInterceptorTest {

    @Autowired
    private JiraProperties properties;
    @Autowired
    private CreateJiraProjectWorker worker;
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
            .expect(requestTo(startsWith(properties.getApiUrl())))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", expectedToken)))
            .andRespond(withStatus(HttpStatus.CREATED));

        String requestUrl = String.format("%s/rest/api/3/project", properties.getApiUrl());

        RequestEntity<Void> requestEntity = RequestEntity
            .post(URI.create(requestUrl))
            .contentType(MediaType.APPLICATION_JSON)
            .build();

        restOperations.exchange(requestEntity, Void.class);

        mockServer.verify();
    }

    @Test
    void workerTest() {
        String usernamePassword = String
            .format("%s:%s", properties.getUsername(), properties.getPassword());
        String expectedToken = Base64.getEncoder().encodeToString(usernamePassword.getBytes());

        mockServer
            .expect(requestTo(startsWith(properties.getApiUrl())))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", expectedToken)))
            .andRespond(withStatus(HttpStatus.CREATED));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "some-project-name");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
    }

    @Import(JiraWorkerConfig.class)
    @Configuration
    public static class ContextConfiguration {

        @Bean
        public JiraProperties jiraProperties() {
            JiraProperties jiraProperties = new JiraProperties();

            jiraProperties.setUsername("some-username");
            jiraProperties.setPassword("some-password");
            jiraProperties.setApiUrl("https://some-api-url");
            jiraProperties.setAccountId("some-account-id");

            return jiraProperties;
        }

    }

}
