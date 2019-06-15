package io.pivotal.conductor.worker.cloudfoundry;

import java.util.Optional;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.spaces.DeleteSpaceRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.spaces.CreateSpaceRequest;
import org.cloudfoundry.operations.spaces.GetSpaceRequest;
import org.cloudfoundry.operations.spaces.SpaceDetail;

public class CloudFoundrySpaceClient {

    private final CloudFoundryOperations cloudFoundryOperations;
    private final CloudFoundryClient cloudFoundryClient;

    public CloudFoundrySpaceClient(
        CloudFoundryOperations cloudFoundryOperations,
        CloudFoundryClient cloudFoundryClient) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.cloudFoundryClient = cloudFoundryClient;
    }

    public void createSpace(String spaceName) {
        CreateSpaceRequest request = CreateSpaceRequest.builder()
            .name(spaceName)
            .build();

        cloudFoundryOperations
            .spaces()
            .create(request)
            .block();
    }

    public Boolean deleteSpace(String spaceName) {
        Optional<String> spaceId = lookupSpaceId(spaceName);
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

    private Optional<String> lookupSpaceId(String spaceName) {
        GetSpaceRequest request = GetSpaceRequest.builder()
            .name(spaceName)
            .build();
        SpaceDetail spaceDetail = cloudFoundryOperations.spaces()
            .get(request)
            .block();

        return Optional.ofNullable(spaceDetail).map(SpaceDetail::getId);
    }

}
