package io.pivotal.conductor.worker.jira;

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

public class CreateJiraProjectWorker implements Worker {

    static final String TASK_DEF_NAME = "create_jira_project";

    private static final String PROJECT_TYPE_KEY = "software";
    private static final String PROJECT_TEMPLATE_KEY = "com.pyxis.greenhopper.jira:gh-scrum-template";

    private final JiraProperties properties;
    private final RestOperations restOperations;
    private final ProjectKeyGenerator projectKeyGenerator;

    public CreateJiraProjectWorker(JiraProperties properties, RestOperations restOperations,
        ProjectKeyGenerator projectKeyGenerator) {
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
            String requestUrl = String.format("%s/rest/api/3/project", properties.getApiUrl());

            CreateJiraProjectRequestDto requestDto =
                new CreateJiraProjectRequestDto(projectKey, projectName, PROJECT_TYPE_KEY,
                    PROJECT_TEMPLATE_KEY, properties.getAccountId());
            RequestEntity<CreateJiraProjectRequestDto> requestEntity = RequestEntity
                .post(URI.create(requestUrl))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);

            restOperations.exchange(requestEntity, Void.class);
        }

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData()
            .put("projectUrl", String.format("%s/projects/%s", properties.getApiUrl(), projectKey));

        return taskResult;
    }

    public static class CreateJiraProjectRequestDto {

        private final String projectKey;
        private final String projectName;
        private final String projectTypeKey;
        private final String projectTemplateKey;
        private final String leadAccountId;

        public CreateJiraProjectRequestDto(String projectKey, String projectName,
            String projectTypeKey, String projectTemplateKey, String leadAccountId) {
            this.projectKey = projectKey;
            this.projectName = projectName;
            this.projectTypeKey = projectTypeKey;
            this.projectTemplateKey = projectTemplateKey;
            this.leadAccountId = leadAccountId;
        }

        @JsonProperty("key")
        public String getProjectKey() {
            return projectKey;
        }

        @JsonProperty("name")
        public String getProjectName() {
            return projectName;
        }

        public String getProjectTypeKey() {
            return projectTypeKey;
        }

        public String getProjectTemplateKey() {
            return projectTemplateKey;
        }

        public String getLeadAccountId() {
            return leadAccountId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CreateJiraProjectRequestDto that = (CreateJiraProjectRequestDto) o;

            return new EqualsBuilder()
                .append(projectKey, that.projectKey)
                .append(projectName, that.projectName)
                .append(projectTypeKey, that.projectTypeKey)
                .append(projectTemplateKey, that.projectTemplateKey)
                .append(leadAccountId, that.leadAccountId)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(projectKey)
                .append(projectName)
                .append(projectTypeKey)
                .append(projectTemplateKey)
                .append(leadAccountId)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("projectKey", projectKey)
                .append("projectName", projectName)
                .append("projectTypeKey", projectTypeKey)
                .append("projectTemplateKey", projectTemplateKey)
                .append("leadAccountId", leadAccountId)
                .toString();
        }
    }
}
