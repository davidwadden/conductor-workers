package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryConfig.CloudFoundryClientsFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;

public class CloudFoundryServiceClient {

    private final CloudFoundryClientsFactory cloudFoundryClientsFactory;

    public CloudFoundryServiceClient(CloudFoundryClientsFactory cloudFoundryClientsFactory) {
        this.cloudFoundryClientsFactory = cloudFoundryClientsFactory;
    }

    public void createServiceInstance(String foundationName, String organizationName,
        String spaceName, String serviceName, String servicePlanName, String serviceInstanceName) {

        CloudFoundryOperations cloudFoundryOperations = cloudFoundryClientsFactory
            .makeOrganizationSpaceOperations(foundationName, organizationName, spaceName);

        CreateServiceInstanceRequest request = CreateServiceInstanceRequest.builder()
            .serviceInstanceName(serviceInstanceName)
            .serviceName(serviceName)
            .planName(servicePlanName)
            .build();

        cloudFoundryOperations.services()
            .createInstance(request)
            .block();
    }

    public void deleteServiceInstance(String foundationName, String organizationName,
        String spaceName, String serviceInstanceName) {

        CloudFoundryOperations cloudFoundryOperations = cloudFoundryClientsFactory
            .makeOrganizationSpaceOperations(foundationName, organizationName, spaceName);

        DeleteServiceInstanceRequest request = DeleteServiceInstanceRequest.builder()
            .name(serviceInstanceName)
            .build();

        cloudFoundryOperations.services()
            .deleteInstance(request)
            .block();
    }
}
