package io.pivotal.conductor.worker.tracker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import java.net.URI;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

public class CreateTrackerProjectWorker implements Worker {

    static final String TASK_DEF_NAME = "create_tracker_project";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PIVOTAL_TRACKER_API_PROJECT_ROOT =
        "https://www.pivotaltracker.com/n/projects/";
    private static final String PIVOTAL_TRACKER_PROJECT_URI =
        "https://www.pivotaltracker.com/services/v5/projects";

    private static final String DEFAULT_PROJECT_TYPE = "public";

    private final TrackerProperties properties;
    private final RestOperations restOperations;

    public CreateTrackerProjectWorker(TrackerProperties properties, RestOperations restOperations) {
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

        Long projectId = -1L;
        if (!dryRun) {
            CreateTrackerProjectRequestDto requestDto =
                new CreateTrackerProjectRequestDto(projectName, DEFAULT_PROJECT_TYPE,
                    properties.getAccountId());

            RequestEntity<CreateTrackerProjectRequestDto> requestEntity = RequestEntity
                .post(URI.create(PIVOTAL_TRACKER_PROJECT_URI))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);
            ResponseEntity<TrackerProjectResponseDto> responseEntity =
                restOperations.exchange(requestEntity, TrackerProjectResponseDto.class);

            projectId = responseEntity.getBody().getProjectId();
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData()
            .put("projectUrl", PIVOTAL_TRACKER_API_PROJECT_ROOT + projectId);

        return taskResult;
    }

    @JsonPropertyOrder({"name", "project_type", "account_id"})
    public static class CreateTrackerProjectRequestDto {

        private final String projectName;
        private final String projectType;
        private final Integer accountId;

        public CreateTrackerProjectRequestDto(String projectName, String projectType,
            Integer accountId) {
            this.projectName = projectName;
            this.projectType = projectType;
            this.accountId = accountId;
        }

        @JsonProperty("name")
        public String getProjectName() {
            return projectName;
        }

        @JsonProperty("project_type")
        public String getProjectType() {
            return projectType;
        }

        @JsonProperty("account_id")
        public Integer getAccountId() {
            return accountId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CreateTrackerProjectRequestDto that = (CreateTrackerProjectRequestDto) o;

            return new EqualsBuilder()
                .append(projectName, that.projectName)
                .append(projectType, that.projectType)
                .append(accountId, that.accountId)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(projectName)
                .append(projectType)
                .append(accountId)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("projectName", projectName)
                .append("projectType", projectType)
                .append("accountId", accountId)
                .toString();
        }
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

            TrackerProjectResponseDto responseDto = (TrackerProjectResponseDto) o;

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
