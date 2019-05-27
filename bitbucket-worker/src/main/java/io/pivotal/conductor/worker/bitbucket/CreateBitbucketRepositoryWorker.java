package io.pivotal.conductor.worker.bitbucket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import java.util.Base64;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestOperations;

public class CreateBitbucketRepositoryWorker implements Worker {

    static final String TASK_DEF_NAME = "create_bitbucket_repository";

    private final BitbucketProperties properties;
    private final RestOperations restOperations;

    public CreateBitbucketRepositoryWorker(BitbucketProperties properties,
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
        String projectKey = (String) task.getInputData().get("projectKey");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));

        String repositoryName = deriveRepositoryName(projectName);
        String repositoryUrl =
            String.format("https://bitbucket.org/%s/%s", properties.getTeamName(), repositoryName);

        if (!dryRun) {
            String requestUrl =
                String.format("https://api.bitbucket.org/2.0/repositories/%s/%s",
                    properties.getTeamName(), repositoryName);
            String usernamePassword =
                String.format("%s:%s", properties.getUsername(), properties.getPassword());
            String authToken = Base64.getEncoder().encodeToString(usernamePassword.getBytes());

            CreateBitbucketRepositoryRequestDto requestDto =
                new CreateBitbucketRepositoryRequestDto(
                    new RepositoryProjectElementDto(projectKey), "git", false);
            RequestEntity<CreateBitbucketRepositoryRequestDto> requestEntity = RequestEntity
                .post(URI.create(requestUrl))
                .header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", authToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);

            restOperations.exchange(requestEntity, Void.class);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("repositoryUrl", repositoryUrl);

        return taskResult;
    }

    private static String deriveRepositoryName(String projectName) {

        return projectName
            .replaceAll("[^\\w\\s]", "")
            .replaceAll("[\\s]", "-")
            .replace("_", "-")
            .replace(".", "-")
            .replaceAll("[^a-zA-Z0-9\\-]", "")
            .toLowerCase();
    }

    public static class CreateBitbucketRepositoryRequestDto {

        private final RepositoryProjectElementDto projectElement;
        private final String sourceControlType;
        private final Boolean privateFlag;

        public CreateBitbucketRepositoryRequestDto(
            RepositoryProjectElementDto projectElement, String sourceControlType,
            Boolean privateFlag) {
            this.projectElement = projectElement;
            this.sourceControlType = sourceControlType;
            this.privateFlag = privateFlag;
        }

        @JsonProperty("project")
        public RepositoryProjectElementDto getProjectElement() {
            return projectElement;
        }

        @JsonProperty("scm")
        public String getSourceControlType() {
            return sourceControlType;
        }

        @JsonProperty("is_private")
        public Boolean getPrivateFlag() {
            return privateFlag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CreateBitbucketRepositoryRequestDto that = (CreateBitbucketRepositoryRequestDto) o;

            return new EqualsBuilder()
                .append(projectElement, that.projectElement)
                .append(sourceControlType, that.sourceControlType)
                .append(privateFlag, that.privateFlag)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(projectElement)
                .append(sourceControlType)
                .append(privateFlag)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("projectElement", projectElement)
                .append("sourceControlType", sourceControlType)
                .append("privateFlag", privateFlag)
                .toString();
        }
    }

    public static class RepositoryProjectElementDto {

        private final String projectKey;

        public RepositoryProjectElementDto(String projectKey) {
            this.projectKey = projectKey;
        }

        @JsonProperty("key")
        public String getProjectKey() {
            return projectKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RepositoryProjectElementDto that = (RepositoryProjectElementDto) o;

            return new EqualsBuilder()
                .append(projectKey, that.projectKey)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(projectKey)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("projectKey", projectKey)
                .toString();
        }
    }

}
