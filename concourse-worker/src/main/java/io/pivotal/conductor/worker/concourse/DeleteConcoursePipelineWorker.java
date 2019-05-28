package io.pivotal.conductor.worker.concourse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

public class DeleteConcoursePipelineWorker implements Worker {

    static final String TASK_DEF_NAME = "delete_concourse_pipeline";

    private final ConcourseProperties properties;
    private final RestOperations restOperations;

    public DeleteConcoursePipelineWorker(
        ConcourseProperties properties,
        RestOperations restOperations) {
        this.properties = properties;
        this.restOperations = restOperations;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String projectName = (String) task.getInputData().get("projectName");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        Boolean wasDeleted = false;
        if (!dryRun) {
            deletePipeline(projectName);
            wasDeleted = true;
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.getOutputData().put("wasDeleted", wasDeleted);
        taskResult.setStatus(Status.COMPLETED);

        return taskResult;
    }

    private void deletePipeline(String projectName) {
        String pipelineName = ConcoursePipelineUtil.derivePipelineName(projectName);
        String bearerToken = getBearerToken();
        destroyPipeline(pipelineName, bearerToken);
    }

    private String getBearerToken() {
        String requestUrl = properties.getApiHost() + "/sky/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>() {{
            add("grant_type", "password");
            add("username", properties.getUsername());
            add("password", properties.getPassword());
            add("scope", "openid profile email federated:id groups");
        }};

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
            .post(URI.create(requestUrl))
            .header(HttpHeaders.AUTHORIZATION, "Basic " + toBase64("fly" + ":" + "Zmx5"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(formData);

        ResponseEntity<TokenResponse> responseEntity =
            restOperations.exchange(requestEntity, TokenResponse.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException(
                "POST " + requestUrl + " returned " + responseEntity.getStatusCode());
        }

        return responseEntity.getBody().getAccessToken();
    }

    private void destroyPipeline(String pipelineName, String bearerToken) {
        String requestUrl =
            properties.getApiHost() + "/api/v1/teams/" + properties.getTeamName() + "/pipelines/"
                + pipelineName;

        RequestEntity requestEntity = RequestEntity
            .delete(URI.create(requestUrl))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .build();

        ResponseEntity<Void> response = restOperations.exchange(requestEntity, Void.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException(
                "DELETE " + requestUrl + " returned " + response.getStatusCode());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TokenResponse {

        private final String accessToken;

        @JsonCreator
        public TokenResponse(@JsonProperty("access_token") String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }

    private static String toBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());

    }
}
