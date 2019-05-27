package io.pivotal.conductor.worker.bitbucket;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import org.springframework.web.client.RestOperations;

public class CreateBitbucketProjectWorker implements Worker {

    static final String TASK_DEF_NAME = "create_bitbucket_project";

    private final BitbucketProperties properties;
    private final RestOperations restOperations;
    private final ProjectKeyGenerator projectKeyGenerator;

    public CreateBitbucketProjectWorker(BitbucketProperties properties,
        RestOperations restOperations, ProjectKeyGenerator projectKeyGenerator) {
        this.properties = properties;
        this.restOperations = restOperations;
        this.projectKeyGenerator = projectKeyGenerator;
    }

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String projectName = (String) task.getInputData().get("projectName");
        Boolean dryRun = Boolean.valueOf((String) task.getInputData().get("dryRun"));
        String projectKey = projectKeyGenerator.generateKey();

        if (!dryRun) {
            // https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories
            String requestUrl =
                String.format("https://api.bitbucket.org/2.0/teams/%s/projects/", properties.getTeamName());

            CreateBitbucketProjectRequestDto requestDto =
                new CreateBitbucketProjectRequestDto(projectName, projectKey, false);
            RequestEntity<CreateBitbucketProjectRequestDto> requestEntity = RequestEntity
                .post(URI.create(requestUrl))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);

            restOperations.exchange(requestEntity, Void.class);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("projectKey", projectKey);

        return taskResult;
    }

    public static class CreateBitbucketProjectRequestDto {

        private final String projectName;
        private final String projectKey;
        private final Boolean privateFlag;

        public CreateBitbucketProjectRequestDto(String projectName, String projectKey,
            Boolean privateFlag) {
            this.projectName = projectName;
            this.projectKey = projectKey;
            this.privateFlag = privateFlag;
        }

        @JsonProperty("name")
        public String getProjectName() {
            return projectName;
        }

        @JsonProperty("key")
        public String getProjectKey() {
            return projectKey;
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

            CreateBitbucketProjectRequestDto that = (CreateBitbucketProjectRequestDto) o;

            return new EqualsBuilder()
                .append(projectName, that.projectName)
                .append(projectKey, that.projectKey)
                .append(privateFlag, that.privateFlag)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(projectName)
                .append(projectKey)
                .append(privateFlag)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("projectName", projectName)
                .append("projectKey", projectKey)
                .append("privateFlag", privateFlag)
                .toString();
        }
    }
}
