package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryConfig.CloudFoundryClientsFactory;
import java.util.Collection;
import java.util.Set;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.uaa.users.CreateUserRequest;
import org.cloudfoundry.uaa.users.DeleteUserRequest;
import org.cloudfoundry.uaa.users.Email;
import org.cloudfoundry.uaa.users.LookupUserIdsRequest;
import org.cloudfoundry.uaa.users.LookupUserIdsResponse;
import org.cloudfoundry.uaa.users.Name;
import org.cloudfoundry.uaa.users.UserId;

public class CloudFoundryUserClient {

    private final CloudFoundryClientsFactory cloudFoundryClientsFactory;

    public CloudFoundryUserClient(
        CloudFoundryClientsFactory cloudFoundryClientsFactory) {
        this.cloudFoundryClientsFactory = cloudFoundryClientsFactory;
    }

    public void createUser(String foundationName, String userName, String password,
        String origin, String externalId) {
        UaaClient uaaClient = cloudFoundryClientsFactory.makeUaaClient(foundationName);

        CreateUserRequest request = CreateUserRequest.builder()
            .email(Email.builder()
                .primary(true)
                .value(userName)
                .build())
            .name(Name.builder()
                .familyName(userName)
                .givenName(userName)
                .build())
            .origin(origin)
            .password(password)
            .userName(userName)
            .externalId(externalId)
            .build();

        uaaClient.users()
            .create(request)
            .block();
    }

    public void deleteUser(String foundationName, String userName) {
        LookupUserIdsResponse lookupResponse = lookupUserIds(foundationName, userName);
        if (lookupResponse.getTotalResults() != 1) {
            throw new IllegalArgumentException(
                String.format("Expected to find 1 user result from lookup, found %d",
                    lookupResponse.getTotalResults()));
        }

        UaaClient uaaClient = cloudFoundryClientsFactory.makeUaaClient(foundationName);

        DeleteUserRequest deleteRequest = DeleteUserRequest.builder()
            .userId(lookupResponse.getResources().get(0).getId())
            .build();

        uaaClient.users()
            .delete(deleteRequest)
            .block();
    }

    public Collection<UserId> listUsers(String foundationName, String userName) {
        LookupUserIdsResponse lookupResponse = lookupUserIds(foundationName, userName);
        return lookupResponse.getResources();
    }

    private LookupUserIdsResponse lookupUserIds(String foundationName, String userName) {
        UaaClient uaaClient = cloudFoundryClientsFactory.makeUaaClient(foundationName);

        LookupUserIdsRequest lookupRequest = LookupUserIdsRequest.builder()
            .filter("userName+eq+\"" + userName + "\"")
            .build();

        return uaaClient.users()
            .lookup(lookupRequest)
            .block();
    }

}
