package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryConfig.CloudFoundryClientsFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;

public class CloudFoundryServiceClient {

    private final CloudFoundryClientsFactory cloudFoundryClientsFactory;

    public CloudFoundryServiceClient(CloudFoundryClientsFactory cloudFoundryClientsFactory) {
        this.cloudFoundryClientsFactory = cloudFoundryClientsFactory;
    }

    public void createMysqlDatabase(String foundationName, String organizationName,
        String spaceName, String serviceInstanceName) {

        CloudFoundryOperations cloudFoundryOperations = cloudFoundryClientsFactory
            .makeOrganizationSpaceOperations(foundationName, organizationName, spaceName);

        CreateServiceInstanceRequest request = CreateServiceInstanceRequest.builder()
            .serviceInstanceName(serviceInstanceName)
            .serviceName("p-mysql")
            .planName("100mb")
            .build();

        cloudFoundryOperations.services()
            .createInstance(request)
            .block();
    }

    public void createRabbitMqBroker(String foundationName, String organizationName,
        String spaceName, String serviceInstanceName) {

        CloudFoundryOperations cloudFoundryOperations = cloudFoundryClientsFactory
            .makeOrganizationSpaceOperations(foundationName, organizationName, spaceName);

        CreateServiceInstanceRequest request = CreateServiceInstanceRequest.builder()
            .serviceInstanceName(serviceInstanceName)
            .serviceName("cloudamqp")
            .planName("lemur")
            .build();

        cloudFoundryOperations.services()
            .createInstance(request)
            .block();
    }

}
