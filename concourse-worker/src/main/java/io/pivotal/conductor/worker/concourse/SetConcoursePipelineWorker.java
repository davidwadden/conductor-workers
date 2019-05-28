package io.pivotal.conductor.worker.concourse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

public class SetConcoursePipelineWorker implements Worker {

    static final String TASK_DEF_NAME = "set_concourse_pipeline";

    // FIXME: this value is duplicated from CommitConcoursePipelineWorker
    private static final String PIPELINE_YML_PATH = "ci/pipeline.yml";

    private final ConcourseProperties properties;
    private final CloudFoundryProperties cloudFoundryProperties;
    private final RestOperations restOperations;

    public SetConcoursePipelineWorker(ConcourseProperties properties,
        CloudFoundryProperties cloudFoundryProperties, RestOperations restOperations) {
        this.properties = properties;
        this.cloudFoundryProperties = cloudFoundryProperties;
        this.restOperations = restOperations;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String projectName = (String) task.getInputData().get("projectName");
        String repositoryUrl = (String) task.getInputData().get("repositoryUrl");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        String pipelineUrl = "https://fake-pipeline-url";
        if (!dryRun) {
            pipelineUrl = setConcoursePipeline(projectName, repositoryUrl);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("pipelineUrl", pipelineUrl);

        return taskResult;
    }

    private String setConcoursePipeline(String projectName, String repositoryUrl) {

        Path pipelineYamlPath = fetchPipelineYml(repositoryUrl);
        String pipelineYaml = interpolatePipelineYml(pipelineYamlPath);
        String pipelineName = ConcoursePipelineUtil.derivePipelineName(projectName);

        // FIXME: obviated with password grant token interceptor
        String bearerToken =
            getBearerToken(properties.getApiHost(), properties.getUsername(),
                properties.getPassword());
        setPipeline(pipelineName, pipelineYaml, bearerToken);
        if (Boolean.valueOf(properties.getShouldExposePipeline())) {
            exposePipeline(pipelineName, bearerToken);
        }
        unpausePipeline(pipelineName, bearerToken);

        return String.format("%s/teams/%s/pipelines/%s",
            properties.getApiHost(), properties.getTeamName(), pipelineName);
    }

    // FIXME: remove Git knowledge from this worker
    private Path fetchPipelineYml(String repositoryUrl) {
        try {
            Path tempDirectory = Files.createTempDirectory("portal");
            Git.cloneRepository()
                .setURI(repositoryUrl)
                .setDirectory(tempDirectory.toFile())
                .call();

            return tempDirectory.resolve(PIPELINE_YML_PATH);
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    // FIXME: remove CF knowledge from this worker
    private String interpolatePipelineYml(Path pipelineYml) {
        try {
            List<String> lines = Files.readAllLines(pipelineYml)
                .stream()
                .map((line) -> line.replace("((cf-username))", cloudFoundryProperties.getUsername()))
                .map((line) -> line.replace("((cf-password))", cloudFoundryProperties.getPassword()))
                .collect(Collectors.toList());

            return String.join("\n", lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getBearerToken(String apiHost, String username, String password) {
        String requestUrl = apiHost + "/sky/token";

        RequestEntity<String> requestEntity = RequestEntity
            .post(URI.create(requestUrl))
            .header(HttpHeaders.AUTHORIZATION, "Basic " + toBase64("fly" + ":" + "Zmx5"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                "grant_type=password&username=" + username + "&password=" + password
                    + "&scope=openid+profile+email+federated:id+groups"
            );

        ResponseEntity<TokenResponse> responseEntity =
            restOperations.exchange(requestEntity, TokenResponse.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            String exceptionMessage =
                "POST " + requestUrl + " returned " + responseEntity.getStatusCode();
            throw new RuntimeException(exceptionMessage);
        }

        return responseEntity.getBody().getAccessToken();
    }

    private void setPipeline(String pipelineName, String pipelineYaml, String bearerToken) {
        String requestUrl =
            String.format("%s/api/v1/teams/%s/pipelines/%s/config", properties.getApiHost(),
                properties.getTeamName(), pipelineName);

        RequestEntity requestEntity = RequestEntity
            .put(URI.create(requestUrl))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .contentType(MediaType.parseMediaType("application/x-yaml"))
            .body(pipelineYaml);

        ResponseEntity<Void> response = restOperations.exchange(requestEntity, Void.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            String exceptionMessage = "PUT " + requestUrl + " returned " + response.getStatusCode();
            throw new RuntimeException(exceptionMessage);
        }
    }

    private void exposePipeline(String pipelineName, String bearerToken) {
        String requestUrl =
            String.format("%s/api/v1/teams/%s/pipelines/%s/expose", properties.getApiHost(),
                properties.getTeamName(), pipelineName);

        RequestEntity requestEntity = RequestEntity
            .put(URI.create(requestUrl))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .build();

        ResponseEntity<Void> response = restOperations.exchange(requestEntity, Void.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            String exceptionMessage = "PUT " + requestUrl + " returned " + response.getStatusCode();
            throw new RuntimeException(exceptionMessage);
        }
    }

    private void unpausePipeline(String pipelineName, String bearerToken) {
        String requestUrl =
            String.format("%s/api/v1/teams/%s/pipelines/%s/unpause", properties.getApiHost(),
                properties.getTeamName(), pipelineName);

        RequestEntity requestEntity = RequestEntity
            .put(URI.create(requestUrl))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .contentType(MediaType.TEXT_PLAIN)
            .body("");

        ResponseEntity<Void> response = restOperations.exchange(requestEntity, Void.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            String exceptionMessage = "PUT " + requestUrl + " returned " + response.getStatusCode();
            throw new RuntimeException(exceptionMessage);
        }
    }

    private static String toBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
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

    public static class CloudFoundryProperties {

        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CloudFoundryProperties that = (CloudFoundryProperties) o;

            return new EqualsBuilder()
                .append(username, that.username)
                .append(password, that.password)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(username)
                .append(password)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("username", username)
                .append("password", password)
                .toString();
        }
    }
}
