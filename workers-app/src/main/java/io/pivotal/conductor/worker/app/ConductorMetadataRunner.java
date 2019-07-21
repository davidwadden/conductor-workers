package io.pivotal.conductor.worker.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.client.exceptions.ConductorClientException;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import java.util.Collections;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ConductorMetadataRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConductorMetadataRunner.class);

    private final MetadataClient metadataClient;
    private final ObjectMapper objectMapper;
    private final Resource[] taskResources;
    private final Resource[] workflowResources;

    public ConductorMetadataRunner(
        MetadataClient metadataClient,
        ObjectMapper objectMapper,
        @Value("classpath:tasks/*.json") Resource[] taskResources,
        @Value("classpath:workflows/*.json") Resource[] workflowResources) {

        this.metadataClient = metadataClient;
        this.objectMapper = objectMapper;
        this.taskResources = taskResources;
        this.workflowResources = workflowResources;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        for (Resource taskResource : taskResources) {
            JsonNode rootNode = objectMapper.readTree(taskResource.getInputStream());
            if (!rootNode.isContainerNode()) {
                throw new IllegalArgumentException("Expected JSON array or object");
            }

            if (rootNode.isArray()) {
                Iterator<JsonNode> iterator = rootNode.elements();
                while (iterator.hasNext()) {
                    JsonNode elementNode = iterator.next();
                    TaskDef taskDef = objectMapper.convertValue(elementNode, TaskDef.class);
                    upsertTaskDef(taskDef);
                }
            } else if (rootNode.isObject()) {
                TaskDef taskDef =
                    objectMapper.readValue(taskResource.getInputStream(), TaskDef.class);
                upsertTaskDef(taskDef);
            }
        }

        for (Resource workflowResource : workflowResources) {
            WorkflowDef workflowDef =
                objectMapper.readValue(workflowResource.getInputStream(), WorkflowDef.class);
            upsertWorkflowDef(workflowDef);
        }
    }

    private void upsertTaskDef(TaskDef taskDef) {
        try {
            metadataClient.updateTaskDef(taskDef);
            logger.info("Updated taskDef: {}", taskDef);
        } catch (ConductorClientException e) {
            metadataClient.registerTaskDefs(Collections.singletonList(taskDef));
            logger.info("Registered taskDef: {}", taskDef);
        }
    }

    private void upsertWorkflowDef(WorkflowDef workflowDef) {
        try {
            metadataClient.updateWorkflowDefs(Collections.singletonList(workflowDef));
            logger.info("Updated workflowDef: {}", workflowDef);
        } catch (ConductorClientException e) {
            metadataClient.registerWorkflowDef(workflowDef);
            logger.info("Registered workflowDef: {}", workflowDef);
        }
    }

}
