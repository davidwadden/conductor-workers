package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryConfig.CloudFoundryClientsFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.useradmin.CreateUserRequest;
import org.cloudfoundry.operations.useradmin.DeleteUserRequest;

public class CloudFoundryUserClient {

    private final CloudFoundryClientsFactory cloudFoundryClientsFactory;

    public CloudFoundryUserClient(
        CloudFoundryClientsFactory cloudFoundryClientsFactory) {
        this.cloudFoundryClientsFactory = cloudFoundryClientsFactory;
    }

    public void createUser(String foundationName, String username, String password) {
        CloudFoundryOperations cloudFoundryOperations =
            cloudFoundryClientsFactory.makeRootOperations(foundationName);

        CreateUserRequest request = CreateUserRequest.builder()
            .username(username)
            .password(password)
            .build();

        cloudFoundryOperations
            .userAdmin()
            .create(request)
            .block();
    }

    public void deleteUser(String foundationName, String username) {
        CloudFoundryOperations cloudFoundryOperations =
            cloudFoundryClientsFactory.makeRootOperations(foundationName);

        DeleteUserRequest request = DeleteUserRequest.builder()
            .username(username)
            .build();

        cloudFoundryOperations
            .userAdmin()
            .delete(request)
            .block();
    }


}
