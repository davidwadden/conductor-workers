package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryConfig.CloudFoundryClientsFactory;
import java.util.Optional;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.spaces.DeleteSpaceRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.spaces.CreateSpaceRequest;
import org.cloudfoundry.operations.spaces.GetSpaceRequest;
import org.cloudfoundry.operations.spaces.SpaceDetail;

public class CloudFoundrySpaceClient {

    private final CloudFoundryClientsFactory cloudFoundryClientsFactory;

    public CloudFoundrySpaceClient(CloudFoundryClientsFactory cloudFoundryClientsFactory) {
        this.cloudFoundryClientsFactory = cloudFoundryClientsFactory;
    }

    public void createSpace(String foundationName, String organizationName, String spaceName) {
        CloudFoundryOperations cloudFoundryOperations =
            cloudFoundryClientsFactory.makeOrganizationOperations(foundationName, organizationName);

        CreateSpaceRequest request = CreateSpaceRequest.builder()
            .name(spaceName)
            .organization(organizationName)
            .build();

        cloudFoundryOperations
            .spaces()
            .create(request)
            .block();
    }

    public Boolean deleteSpace(String foundationName, String organizationName, String spaceName) {
        CloudFoundryClient cloudFoundryClient =
            cloudFoundryClientsFactory.makeClient(foundationName);

        Optional<String> spaceId = lookupSpaceId(foundationName, organizationName, spaceName);
        if (!spaceId.isPresent()) {
            return false;
        }

        DeleteSpaceRequest request = DeleteSpaceRequest.builder()
            .spaceId(spaceId.get())
            .recursive(true)
            .build();

        cloudFoundryClient.spaces()
            .delete(request)
            .block();

        return true;
    }

    private Optional<String> lookupSpaceId(String foundationName, String organizationName,
        String spaceName) {
        CloudFoundryOperations cloudFoundryOperations =
            cloudFoundryClientsFactory.makeOrganizationOperations(foundationName, organizationName);

        GetSpaceRequest request = GetSpaceRequest.builder()
            .name(spaceName)
            .build();
        SpaceDetail spaceDetail = cloudFoundryOperations.spaces()
            .get(request)
            .block();

        return Optional.ofNullable(spaceDetail)
            .map(SpaceDetail::getId);
    }

}
