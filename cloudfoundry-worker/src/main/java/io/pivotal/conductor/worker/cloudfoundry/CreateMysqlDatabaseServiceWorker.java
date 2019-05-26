package io.pivotal.conductor.worker.cloudfoundry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;

public class CreateMysqlDatabaseServiceWorker implements Worker {

    static final String TASK_DEF_NAME = "create_mysql_database";

    @Override
    public String getTaskDefName() {
        return TASK_DEF_NAME;
    }

    @Override
    public TaskResult execute(Task task) {
        String projectName = (String) task.getInputData().get("projectName");
        String spaceNameSuffix = (String) task.getInputData().get("spaceNameSuffix");
        String spaceName = (String) task.getInputData().get("spaceName");
        String databaseName = CloudFoundryUtil.deriveDatabaseName(projectName, spaceNameSuffix);

        // create mysql database service on cloud foundry

        TaskResult taskResult = new TaskResult(task);
        taskResult.setStatus(Status.COMPLETED);
        taskResult.getOutputData().put("databaseName", databaseName);

        return taskResult;
    }
}
