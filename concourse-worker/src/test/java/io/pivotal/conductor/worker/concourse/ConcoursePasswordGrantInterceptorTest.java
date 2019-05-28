package io.pivotal.conductor.worker.concourse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(classes = {
    ConcoursePasswordGrantInterceptorTest.ContextConfiguration.class,
})
@ExtendWith(SpringExtension.class)
class ConcoursePasswordGrantInterceptorTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ConcourseProperties properties;
    @Autowired
    private DeleteConcoursePipelineWorker worker;
    @Autowired
    private RestOperations concourseRestOperations;
    @Autowired
    private RestOperations concourseTokenRestOperations;
    private MockRestServiceServer mockServer;
    private MockRestServiceServer mockTokenServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer((RestTemplate) concourseRestOperations);
        mockTokenServer = MockRestServiceServer
            .createServer((RestTemplate) concourseTokenRestOperations);
    }

    @Test
    void interceptorTest() throws JsonProcessingException {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>() {{
            add("grant_type", "password");
            add("username", "some-username");
            add("password", "some-password");
            add("scope", "openid profile email federated:id groups");
        }};

        Map<String, Object> responseDto = Map.of(
            "access_token", "some-access-token",
            "token_type", "Bearer",
            "expiry", "2019-05-29T00:11:26Z"
        );
        String responseBody = objectMapper.writeValueAsString(responseDto);

        String authToken =
            Base64.getEncoder().encodeToString(("fly" + ":" + "Zmx5").getBytes());
        mockTokenServer
            .expect(requestTo("https://some-api-host/sky/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", authToken)))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(content().formData(formData))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        String requestUrl = String.format("%s/some-api-endpoint", properties.getApiHost());

        mockServer
            .expect(requestTo(requestUrl))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer some-access-token"))
            .andRespond(withNoContent());

        RequestEntity<Void> requestEntity = RequestEntity
            .delete(URI.create(requestUrl))
            .build();

        concourseRestOperations.exchange(requestEntity, Void.class);

        mockTokenServer.verify();
        mockServer.verify();
    }

    @Test
    void workerTest() throws JsonProcessingException {
        properties.setApiHost("https://some-api-host");
        properties.setTeamName("some-team-name");
        properties.setUsername("some-username");
        properties.setPassword("some-password");
        properties.setShouldExposePipeline("false");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>() {{
            add("grant_type", "password");
            add("username", "some-username");
            add("password", "some-password");
            add("scope", "openid profile email federated:id groups");
        }};

        Map<String, Object> responseDto = Map.of(
            "access_token", "some-access-token",
            "token_type", "Bearer",
            "expiry", "2019-05-29T00:11:26Z"
        );
        String responseBody = objectMapper.writeValueAsString(responseDto);

        String authToken =
            Base64.getEncoder().encodeToString(("fly" + ":" + "Zmx5").getBytes());
        mockTokenServer
            .expect(requestTo("https://some-api-host/sky/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", authToken)))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(content().formData(formData))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        String requestUrl =
            String.format("%s/api/v1/teams/%s/pipelines/some-project-name",
                properties.getApiHost(), properties.getTeamName());
        mockServer
            .expect(requestTo(requestUrl))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer some-access-token"))
            .andRespond(withNoContent());

        Task task = new Task();
        task.setStatus(Task.Status.SCHEDULED);
        Map<String, Object> inputData = Map.of("projectName", "Some Project Name!");
        task.setInputData(inputData);

        TaskResult taskResult = worker.execute(task);

        mockTokenServer.verify();
        mockServer.verify();

        assertThat(taskResult.getStatus()).isEqualTo(TaskResult.Status.COMPLETED);
    }

    @Import(ConcourseWorkerConfig.class)
    @Configuration
    public static class ContextConfiguration {

        @Bean
        public ConcourseProperties ConcourseProperties() {
            ConcourseProperties properties = new ConcourseProperties();

            properties.setApiHost("https://some-api-host");
            properties.setTeamName("some-team-name");
            properties.setUsername("some-username");
            properties.setPassword("some-password");
            properties.setShouldExposePipeline("false");

            return properties;
        }
    }

}
