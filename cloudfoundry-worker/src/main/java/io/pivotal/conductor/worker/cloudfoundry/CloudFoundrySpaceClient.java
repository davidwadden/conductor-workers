package io.pivotal.conductor.worker.cloudfoundry;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.spaces.CreateSpaceRequest;

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

}
