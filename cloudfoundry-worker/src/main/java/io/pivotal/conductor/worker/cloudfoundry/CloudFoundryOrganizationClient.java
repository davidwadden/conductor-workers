package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryConfig.CloudFoundryClientsFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.organizations.CreateOrganizationRequest;
import org.cloudfoundry.operations.organizations.DeleteOrganizationRequest;

public class CloudFoundryOrganizationClient {

    private final CloudFoundryClientsFactory cloudFoundryClientsFactory;

    public CloudFoundryOrganizationClient(CloudFoundryClientsFactory cloudFoundryClientsFactory) {
        this.cloudFoundryClientsFactory = cloudFoundryClientsFactory;
    }

    public void createOrganization(String foundationName, String organizationName) {
        CloudFoundryOperations cloudFoundryOperations =
            cloudFoundryClientsFactory.makeOrganizationOperations(foundationName, organizationName);

        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
            .organizationName(organizationName)
            .build();

        cloudFoundryOperations
            .organizations()
            .create(request)
            .block();
    }

    public void deleteOrganization(String foundationName, String organizationName) {
        CloudFoundryOperations cloudFoundryOperations =
            cloudFoundryClientsFactory.makeRootOperations(foundationName);

        DeleteOrganizationRequest request = DeleteOrganizationRequest.builder()
            .name(organizationName)
            .build();

        cloudFoundryOperations
            .organizations()
            .delete(request)
            .block();
    }
}
