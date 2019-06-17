package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryConfig.CloudFoundryClientsFactory;
import java.util.Optional;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.routes.CreateRouteRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.domains.Domain;
import org.cloudfoundry.operations.spaces.GetSpaceRequest;
import org.cloudfoundry.operations.spaces.SpaceDetail;

public class CloudFoundryRouteClient {

    private final CloudFoundryClientsFactory cloudFoundryClientsFactory;

    public CloudFoundryRouteClient(CloudFoundryClientsFactory cloudFoundryClientsFactory) {
        this.cloudFoundryClientsFactory = cloudFoundryClientsFactory;
    }

    public void createRoute(String foundationName, String organizationName, String spaceName,
        String hostName, String domainName) {

        Optional<String> spaceId = lookupSpaceId(foundationName, organizationName, spaceName);
        Optional<String> domainId = lookupDomainId(foundationName, organizationName, domainName);
        if (!spaceId.isPresent() || !domainId.isPresent()) {
            return;
        }

        CreateRouteRequest request = CreateRouteRequest.builder()
            .spaceId(spaceId.get())
            .host(hostName)
            .domainId(domainId.get())
            .build();

        CloudFoundryClient cloudFoundryClient =
            cloudFoundryClientsFactory.makeClient(foundationName);

        cloudFoundryClient.routes()
            .create(request)
            .block();
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

        return Optional.ofNullable(spaceDetail).map(SpaceDetail::getId);
    }

    private Optional<String> lookupDomainId(String foundationName, String organizationName,
        String domainName) {
        CloudFoundryOperations cloudFoundryOperations =
            cloudFoundryClientsFactory.makeOrganizationOperations(foundationName, organizationName);

        Domain domain = cloudFoundryOperations.domains()
            .list()
            .filter(d -> domainName.equals(d.getName()))
            .blockFirst();

        return Optional.ofNullable(domain).map(Domain::getId);
    }

}
