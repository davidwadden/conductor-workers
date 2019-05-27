package io.pivotal.conductor.worker.jira;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

public class DeleteJiraProjectWorker implements Worker {

    static final String TASK_DEF_NAME = "delete_jira_project";

    private final JiraProperties properties;
    private final RestOperations restOperations;

    public DeleteJiraProjectWorker(JiraProperties properties, RestOperations restOperations) {
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
            String searchProjectsRequestUrl =
                String.format("%s/rest/api/3/project/search?startAt=0&maxResults=100",
                    properties.getApiUrl());
            RequestEntity<Void> searchProjectsRequestEntity = RequestEntity
                .get(URI.create(searchProjectsRequestUrl))
                .build();

            ResponseEntity<SearchProjectsResponseDto> searchProjectsResponseEntity =
                restOperations.exchange(searchProjectsRequestEntity, SearchProjectsResponseDto.class);

            Optional<ProjectSearchResultDto> searchResult = searchProjectsResponseEntity
                .getBody()
                .getSearchResults()
                .stream()
                .filter(result -> projectName.equals(result.getProjectName()))
                .findAny();

            if (searchResult.isEmpty()) {
                TaskResult taskResult = new TaskResult(task);
                taskResult.setStatus(Status.COMPLETED);
                taskResult.getOutputData().put("wasDeleted", wasDeleted);
                return taskResult;
            }

            String projectKeyToDelete = searchResult.get().getProjectKey();
            String deleteProjectRequestUrl =
                String.format("%s/rest/api/3/project/%s", properties.getApiUrl(), projectKeyToDelete);

            RequestEntity<Void> deleteProjectRequestEntity = RequestEntity
                .delete(URI.create(deleteProjectRequestUrl))
                .build();
            restOperations.exchange(deleteProjectRequestEntity, Void.class);

            wasDeleted = true;
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("wasDeleted", wasDeleted);

        return taskResult;
    }

    public static class SearchProjectsResponseDto {

        private final List<ProjectSearchResultDto> searchResults;

        @JsonCreator
        public SearchProjectsResponseDto(
            @JsonProperty("values") List<ProjectSearchResultDto> searchResults) {
            this.searchResults = searchResults;
        }

        @JsonProperty("values")
        public List<ProjectSearchResultDto> getSearchResults() {
            return searchResults;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SearchProjectsResponseDto that = (SearchProjectsResponseDto) o;

            return new EqualsBuilder()
                .append(searchResults, that.searchResults)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(searchResults)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("searchResults", searchResults)
                .toString();
        }
    }

    public static class ProjectSearchResultDto {

        private final String projectKey;
        private final String projectName;

        @JsonCreator
        public ProjectSearchResultDto(@JsonProperty("key") String projectKey,
            @JsonProperty("name") String projectName) {
            this.projectKey = projectKey;
            this.projectName = projectName;
        }

        @JsonProperty("key")
        public String getProjectKey() {
            return projectKey;
        }

        @JsonProperty("name")
        public String getProjectName() {
            return projectName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ProjectSearchResultDto that = (ProjectSearchResultDto) o;

            return new EqualsBuilder()
                .append(projectKey, that.projectKey)
                .append(projectName, that.projectName)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(projectKey)
                .append(projectName)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("projectKey", projectKey)
                .append("projectName", projectName)
                .toString();
        }
    }

}
