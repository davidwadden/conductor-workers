package io.pivotal.conductor.worker.tracker;

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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

public class DeleteTrackerProjectWorker implements Worker {

    static final String TASK_DEF_NAME = "delete_tracker_project";

    private static final URI PIVOTAL_TRACKER_PROJECT_URI =
        URI.create("https://www.pivotaltracker.com/services/v5/projects");

    private final TrackerProperties properties;
    private final RestOperations restOperations;

    public DeleteTrackerProjectWorker(TrackerProperties properties, RestOperations restOperations) {
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
            URI getProjectRequestUri = UriComponentsBuilder
                .fromUri(PIVOTAL_TRACKER_PROJECT_URI)
                .queryParam("account_ids", properties.getAccountId())
                .build()
                .toUri();

            RequestEntity<Void> requestEntity = RequestEntity
                .get(getProjectRequestUri)
                .build();

            //noinspection Convert2Diamond
            ResponseEntity<List<TrackerProjectResponseDto>> getProjectResponseEntity =
                restOperations.exchange(requestEntity, new ParameterizedTypeReference<List<TrackerProjectResponseDto>>() {
                });

            Optional<TrackerProjectResponseDto> project = getProjectResponseEntity.getBody()
                .stream()
                .filter(p -> projectName.equals(p.getProjectName()))
                .findAny();

            if (!project.isPresent()) {
                TaskResult taskResult = new TaskResult(task);
                taskResult.setStatus(Status.COMPLETED);
                taskResult.getOutputData().put("wasDeleted", false);
                return taskResult;
            }

            URI deleteProjectRequestUri = UriComponentsBuilder.fromUri(PIVOTAL_TRACKER_PROJECT_URI)
                .path("/" + project.get().getProjectId())
                .build().toUri();

            RequestEntity<Void> deleteProjectRequestEntity = RequestEntity
                .delete(deleteProjectRequestUri)
                .build();

            restOperations.exchange(deleteProjectRequestEntity, Void.class);
            wasDeleted = true;
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("wasDeleted", wasDeleted);

        return taskResult;
    }

    public static class TrackerProjectResponseDto {

        private final Long projectId;
        private final String projectName;

        @JsonCreator
        public TrackerProjectResponseDto(@JsonProperty("id") Long projectId,
            @JsonProperty("name") String projectName) {
            this.projectId = projectId;
            this.projectName = projectName;
        }

        @JsonProperty("id")
        public Long getProjectId() {
            return projectId;
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

            DeleteTrackerProjectWorker.TrackerProjectResponseDto responseDto = (DeleteTrackerProjectWorker.TrackerProjectResponseDto) o;

            return new EqualsBuilder()
                .append(projectId, responseDto.projectId)
                .append(projectName, responseDto.projectName)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(projectId)
                .append(projectName)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("projectId", projectId)
                .append("projectName", projectName)
                .toString();
        }
    }

}
