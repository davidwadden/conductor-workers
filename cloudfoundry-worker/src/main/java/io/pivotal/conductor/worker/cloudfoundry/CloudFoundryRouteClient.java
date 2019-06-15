package io.pivotal.conductor.worker.cloudfoundry;

import java.util.Optional;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.routes.CreateRouteRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.domains.Domain;
import org.cloudfoundry.operations.spaces.GetSpaceRequest;
import org.cloudfoundry.operations.spaces.SpaceDetail;

public class CloudFoundryRouteClient {

    private final CloudFoundryProperties properties;
    private final CloudFoundryOperations cloudFoundryOperations;
    private final CloudFoundryClient cloudFoundryClient;

    public CloudFoundryRouteClient(
        CloudFoundryProperties properties,
        CloudFoundryOperations cloudFoundryOperations,
        CloudFoundryClient cloudFoundryClient) {
        this.properties = properties;
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.cloudFoundryClient = cloudFoundryClient;
    }

    public void createRoute(String spaceName, String hostname, String domainName) {
        Optional<String> spaceId = lookupSpaceId(spaceName);
        Optional<String> domainId = lookupDomainId(domainName);
        if (!spaceId.isPresent() || !domainId.isPresent()) {
            return;
        }

        CreateRouteRequest request = CreateRouteRequest.builder()
            .spaceId(spaceId.get())
            .host(hostname)
            .domainId(domainId.get())
            .build();

        cloudFoundryClient.routes()
            .create(request)
            .block();
    }

    private Optional<String> lookupSpaceId(String spaceName) {
        GetSpaceRequest request = GetSpaceRequest.builder()
            .name(spaceName)
            .build();
        SpaceDetail spaceDetail = cloudFoundryOperations.spaces()
            .get(request)
            .block();

        return Optional.ofNullable(spaceDetail).map(SpaceDetail::getId);
    }

    private Optional<String> lookupDomainId(String domainName) {
        Domain domain = cloudFoundryOperations.domains()
            .list()
            .filter(d -> domainName.equals(d.getName()))
            .blockFirst();

        return Optional.ofNullable(domain).map(Domain::getId);
    }

}
