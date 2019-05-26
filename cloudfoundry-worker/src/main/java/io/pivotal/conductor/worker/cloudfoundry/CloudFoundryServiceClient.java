package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryConfig.SpaceScopedCloudFoundryOperationsFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;

public class CloudFoundryServiceClient {

    private final SpaceScopedCloudFoundryOperationsFactory spaceScopedCloudFoundryOperationsFactory;

    public CloudFoundryServiceClient(
        SpaceScopedCloudFoundryOperationsFactory spaceScopedCloudFoundryOperationsFactory) {
        this.spaceScopedCloudFoundryOperationsFactory = spaceScopedCloudFoundryOperationsFactory;
    }

    public void createMysqlDatabase(String databaseName, String spaceName) {
        CloudFoundryOperations cloudFoundryOperations =
            spaceScopedCloudFoundryOperationsFactory.makeCloudFoundryOperations(spaceName);

        CreateServiceInstanceRequest request = CreateServiceInstanceRequest.builder()
            .serviceInstanceName(databaseName)
            .serviceName("p-mysql")
            .planName("100mb")
            .build();

        cloudFoundryOperations.services()
            .createInstance(request)
            .block();
    }

}
