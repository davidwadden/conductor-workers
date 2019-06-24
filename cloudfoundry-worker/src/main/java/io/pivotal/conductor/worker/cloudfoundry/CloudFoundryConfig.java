package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryProperties.CloudFoundryFoundationProperties;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudFoundryConfig {

    @Autowired
    private CloudFoundryProperties properties;

    @Bean
    public CloudFoundryClientsFactory cloudFoundryClientsFactory() {
        return new CloudFoundryClientsFactory(cloudFoundryClientsMap());
    }

    @Bean
    public CloudFoundryOrganizationClient cloudFoundryOrganizationClient() {
        return new CloudFoundryOrganizationClient(cloudFoundryClientsFactory());
    }

    @Bean
    public CloudFoundrySpaceClient cloudFoundrySpaceClient() {
        return new CloudFoundrySpaceClient(cloudFoundryClientsFactory());
    }

    @Bean
    public CloudFoundryServiceClient cloudFoundryServiceClient() {
        return new CloudFoundryServiceClient(cloudFoundryClientsFactory());
    }

    @Bean
    public CloudFoundryUserClient cloudFoundryUserClient() {
        return new CloudFoundryUserClient(cloudFoundryClientsFactory());
    }

    @Bean
    public Map<String, CloudFoundryClients> cloudFoundryClientsMap() {
        return properties.getFoundations()
            .entrySet()
            .stream()
            .map(entry -> {
                CloudFoundryFoundationProperties foundationProperties = entry.getValue();
                ConnectionContext connectionContext = DefaultConnectionContext.builder()
                    .apiHost(foundationProperties.getApiHost())
                    .skipSslValidation(foundationProperties.getSkipSslValidation())
                    .build();

                TokenProvider tokenProvider;
                if (StringUtils.isNotEmpty(foundationProperties.getClientId()) &&
                    StringUtils.isNotEmpty(foundationProperties.getClientSecret())) {
                    tokenProvider = ClientCredentialsGrantTokenProvider.builder()
                        .clientId(foundationProperties.getClientId())
                        .clientSecret(foundationProperties.getClientSecret())
                        .scope(foundationProperties.getScope())
                        .build();
                } else if (StringUtils.isNotEmpty(foundationProperties.getUsername()) &&
                    StringUtils.isNotEmpty(foundationProperties.getPassword())) {
                    tokenProvider = PasswordGrantTokenProvider.builder()
                        .password(foundationProperties.getPassword())
                        .username(foundationProperties.getUsername())
                        .build();
                } else {
                    throw new IllegalArgumentException("Unknown OAuth configuration");
                }

                CloudFoundryClient cloudFoundryClient = ReactorCloudFoundryClient.builder()
                    .connectionContext(connectionContext)
                    .tokenProvider(tokenProvider)
                    .build();

                DopplerClient dopplerClient = ReactorDopplerClient.builder()
                    .connectionContext(connectionContext)
                    .tokenProvider(tokenProvider)
                    .build();

                UaaClient uaaClient = ReactorUaaClient.builder()
                    .connectionContext(connectionContext)
                    .tokenProvider(tokenProvider)
                    .build();

                DefaultCloudFoundryOperations.Builder cloudFoundryOperationsBuilder =
                    DefaultCloudFoundryOperations.builder()
                        .cloudFoundryClient(cloudFoundryClient)
                        .dopplerClient(dopplerClient)
                        .uaaClient(uaaClient);

                CloudFoundryOperations cloudFoundryOperations =
                    DefaultCloudFoundryOperations.builder()
                        .cloudFoundryClient(cloudFoundryClient)
                        .dopplerClient(dopplerClient)
                        .uaaClient(uaaClient)
                        .build();

                CloudFoundryClients cloudFoundryClients = new CloudFoundryClients(connectionContext,
                    tokenProvider, cloudFoundryClient,
                    dopplerClient, uaaClient, cloudFoundryOperationsBuilder,
                    cloudFoundryOperations);
                return new AbstractMap.SimpleEntry<>(entry.getKey(),
                    cloudFoundryClients);
            })
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

    public static class CloudFoundryClientsFactory {

        private final Map<String, CloudFoundryClients> cloudFoundryClientsMap;

        public CloudFoundryClientsFactory(
            Map<String, CloudFoundryClients> cloudFoundryClientsMap) {
            this.cloudFoundryClientsMap = cloudFoundryClientsMap;
        }

        public CloudFoundryClient makeClient(String foundationName) {
            CloudFoundryClients foundationClients = cloudFoundryClientsMap.get(foundationName);
            if (foundationClients == null) {
                throw new IllegalArgumentException("Invalid foundationName");
            }

            return foundationClients.getCloudFoundryClient();
        }

        public CloudFoundryOperations makeRootOperations(String foundationName) {
            CloudFoundryClients foundationClients = cloudFoundryClientsMap.get(foundationName);
            if (foundationClients == null) {
                throw new IllegalArgumentException("Invalid foundationName");
            }

            return foundationClients.getCloudFoundryOperations();
        }

        public CloudFoundryOperations makeOrganizationOperations(
            String foundationName, String organizationName) {

            CloudFoundryClients foundationClients = cloudFoundryClientsMap.get(foundationName);
            if (foundationClients == null) {
                throw new IllegalArgumentException("Invalid foundationName");
            }

            return DefaultCloudFoundryOperations.builder()
                .from((DefaultCloudFoundryOperations) foundationClients.getCloudFoundryOperations())
                .organization(organizationName)
                .build();
        }

        public CloudFoundryOperations makeOrganizationSpaceOperations(
            String foundationName, String organizationName, String spaceName) {

            CloudFoundryClients foundationClients = cloudFoundryClientsMap.get(foundationName);
            if (foundationClients == null) {
                throw new IllegalArgumentException("Invalid foundationName");
            }

            return DefaultCloudFoundryOperations.builder()
                .from((DefaultCloudFoundryOperations) foundationClients.getCloudFoundryOperations())
                .organization(organizationName)
                .space(spaceName)
                .build();
        }

        public UaaClient makeUaaClient(String foundationName) {
            CloudFoundryClients foundationClients = cloudFoundryClientsMap.get(foundationName);
            if (foundationClients == null) {
                throw new IllegalArgumentException("Invalid foundationName");
            }

            return foundationClients.getUaaClient();
        }
    }

}
