package io.pivotal.conductor.worker.bitbucket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

public class DeleteBitbucketProjectWorker implements Worker {

    static final String TASK_DEF_NAME = "delete_bitbucket_project";

    private final BitbucketProperties properties;
    private final RestOperations restOperations;

    public DeleteBitbucketProjectWorker(BitbucketProperties properties,
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
            String listProjectsRequestUrl =
                String.format("https://api.bitbucket.org/2.0/teams/%s/projects/?pagelen=100", properties.getTeamName());
            RequestEntity<Void> listProjectsRequestEntity = RequestEntity
                .get(URI.create(listProjectsRequestUrl))
                .build();

            ResponseEntity<SearchProjectsResponseDto> listProjectsResponseEntity =
                restOperations.exchange(listProjectsRequestEntity, SearchProjectsResponseDto.class);

            Optional<String> projectKey = listProjectsResponseEntity.getBody()
                .getSearchResults()
                .stream()
                .filter(searchResult -> projectName.equals(searchResult.getProjectName()))
                .map(ProjectSearchResultDto::getProjectKey)
                .findAny();

            if (projectKey.isEmpty()) {
                TaskResult taskResult = new TaskResult(task);
                taskResult.setStatus(Status.COMPLETED);
                taskResult.getOutputData().put("wasDeleted", wasDeleted);
                return taskResult;
            }

            String deleteProjectRequestUrl =
                String.format("https://api.bitbucket.org/2.0/teams/%s/projects/%s", properties.getTeamName(), projectKey.get());

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
