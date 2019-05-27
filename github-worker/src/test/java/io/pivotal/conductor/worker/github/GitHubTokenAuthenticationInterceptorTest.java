package io.pivotal.conductor.worker.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import io.pivotal.conductor.worker.github.CreateGitHubRepositoryWorker.CreateRepositoryResponseDto;
import java.io.IOException;
import java.net.URI;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(classes = {
    GitHubTokenAuthenticationInterceptorTest.ContextConfiguration.class,
})
@ExtendWith(SpringExtension.class)
class GitHubTokenAuthenticationInterceptorTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GitHubProperties properties;
    @Autowired
    private CreateGitHubRepositoryWorker worker;
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
            .expect(requestTo(startsWith("https://api.github.com")))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "token some-api-key"))
            .andRespond(withSuccess());

        String requestUrl = "https://api.github.com/some-api-endpoint";

        RequestEntity<Void> requestEntity = RequestEntity
            .get(URI.create(requestUrl))
            .build();

        restOperations.exchange(requestEntity, Void.class);

        mockServer.verify();
    }

    @Test
    void workerTest() throws IOException {
        CreateRepositoryResponseDto responseDto =
            new CreateRepositoryResponseDto("http://github.com/some-org-name/some-repository-name");
        String responseBody = objectMapper.writeValueAsString(responseDto);

        mockServer
            .expect(requestTo("https://api.github.com/orgs/some-org-name/repos"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "token some-api-key"))
            .andExpect(header(HttpHeaders.ACCEPT, "application/vnd.github.v3+json"))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = new HashMap<>() {{
            put("projectName", "Some Repository Name!");
        }};
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
        assertThat(taskResult.getOutputData())
            .containsEntry("repositoryUrl", "http://github.com/some-org-name/some-repository-name");
    }

    @Import(GitHubWorkerConfig.class)
    @Configuration
    public static class ContextConfiguration {

        @Bean
        public GitHubProperties GitHubProperties() {
            GitHubProperties gitHubProperties = new GitHubProperties();

            gitHubProperties.setUsername("some-username");
            gitHubProperties.setToken("some-api-key");
            gitHubProperties.setOrganizationName("some-org-name");

            return gitHubProperties;
        }
    }
}
