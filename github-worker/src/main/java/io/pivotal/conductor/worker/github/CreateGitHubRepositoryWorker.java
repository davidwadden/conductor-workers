package io.pivotal.conductor.worker.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

public class CreateGitHubRepositoryWorker implements Worker {

    static final String TASK_DEF_NAME = "create_github_repository";

    private final GitHubProperties properties;
    private final RestOperations restOperations;

    public CreateGitHubRepositoryWorker(GitHubProperties properties,
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
        String repositoryName = GitHubRepositoryUtil.deriveRepositoryName(projectName);

        // generate default repository url in case of dry run
        String repositoryUrl =
            String.format("http://github.com/%s/%s", properties.getOrganizationName(), repositoryName);

        if (!dryRun) {
            String requestUrl =
                String.format("https://api.github.com/orgs/%s/repos", properties.getOrganizationName());
            RequestEntity<CreateRepositoryRequestDto> requestEntity = RequestEntity
                .post(URI.create(requestUrl))
                .header(HttpHeaders.AUTHORIZATION, "token " + properties.getToken())
                .header(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateRepositoryRequestDto(repositoryName));

            ResponseEntity<CreateRepositoryResponseDto> responseEntity =
                restOperations.exchange(requestEntity, CreateRepositoryResponseDto.class);
            repositoryUrl = responseEntity.getBody().getHtmlUrl();
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("repositoryUrl", repositoryUrl);

        return taskResult;
    }

    public static class CreateRepositoryRequestDto {

        private final String name;

        public CreateRepositoryRequestDto(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CreateRepositoryRequestDto that = (CreateRepositoryRequestDto) o;

            return new EqualsBuilder()
                .append(name, that.name)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("name", name)
                .toString();
        }
    }

    public static class CreateRepositoryResponseDto {

        private final String htmlUrl;

        @JsonCreator
        public CreateRepositoryResponseDto(@JsonProperty("html_url") String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }

        @JsonProperty("html_url")
        public String getHtmlUrl() {
            return htmlUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CreateRepositoryResponseDto that = (CreateRepositoryResponseDto) o;

            return new EqualsBuilder()
                .append(htmlUrl, that.htmlUrl)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(htmlUrl)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("htmlUrl", htmlUrl)
                .toString();
        }
    }

}
