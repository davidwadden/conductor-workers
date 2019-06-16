package io.pivotal.conductor.worker.cloudfoundry;

import io.pivotal.conductor.worker.cloudfoundry.CloudFoundryProperties.CloudFoundryFoundationProperties;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudFoundryConfig {

    public static final String DEFAULT_FOUNDATION_NAME = "default";

    @Autowired
    private CloudFoundryProperties properties;

    @Bean
    public CloudFoundrySpaceClient cloudFoundrySpaceClient() {
        return new CloudFoundrySpaceClient(defaultOrganizationCloudFoundryOperations(),
            cloudFoundryClient());
    }

    @Bean
    public CloudFoundryRouteClient cloudFoundryRouteClient() {
        return new CloudFoundryRouteClient(properties, defaultOrganizationCloudFoundryOperations(),
            cloudFoundryClient());
    }

    @Bean
    public CloudFoundryServiceClient cloudFoundryServiceClient(
        SpaceScopedCloudFoundryOperationsFactory spaceScopedCloudFoundryOperationsFactory) {
        return new CloudFoundryServiceClient(spaceScopedCloudFoundryOperationsFactory);
    }

    @Bean
    public ConnectionContext connectionContext() {
        CloudFoundryFoundationProperties defaultFoundationProperties =
            properties.getFoundations().get(DEFAULT_FOUNDATION_NAME);
        return DefaultConnectionContext.builder()
            .apiHost(defaultFoundationProperties.getApiHost())
            .skipSslValidation(defaultFoundationProperties.getSkipSslValidation())
            .build();
    }

    @Bean
    public TokenProvider tokenProvider() {
        CloudFoundryFoundationProperties defaultFoundationProperties =
            properties.getFoundations().get(DEFAULT_FOUNDATION_NAME);
        return PasswordGrantTokenProvider.builder()
            .password(defaultFoundationProperties.getPassword())
            .username(defaultFoundationProperties.getUsername())
            .build();
    }

    @Bean
    public CloudFoundryClient cloudFoundryClient() {
        return ReactorCloudFoundryClient.builder()
            .connectionContext(connectionContext())
            .tokenProvider(tokenProvider())
            .build();
    }

    @Bean
    public DopplerClient dopplerClient() {
        return ReactorDopplerClient.builder()
            .connectionContext(connectionContext())
            .tokenProvider(tokenProvider())
            .build();
    }

    @Bean
    public UaaClient uaaClient() {
        return ReactorUaaClient.builder()
            .connectionContext(connectionContext())
            .tokenProvider(tokenProvider())
            .build();
    }

    @Bean
    public DefaultCloudFoundryOperations.Builder defaultOrganizationCloudFoundryOperationsBuilder() {
        CloudFoundryFoundationProperties defaultFoundationProperties =
            properties.getFoundations().get(DEFAULT_FOUNDATION_NAME);
        return DefaultCloudFoundryOperations.builder()
            .cloudFoundryClient(cloudFoundryClient())
            .dopplerClient(dopplerClient())
            .uaaClient(uaaClient())
            .organization(defaultFoundationProperties.getOrganization());
    }

    @Bean
    public CloudFoundryOperations defaultOrganizationCloudFoundryOperations() {
        return defaultOrganizationCloudFoundryOperationsBuilder().build();
    }

    @Bean
    public SpaceScopedCloudFoundryOperationsFactory spaceScopedCloudFoundryOperationsFactory() {
        return new SpaceScopedCloudFoundryOperationsFactory(
            defaultOrganizationCloudFoundryOperationsBuilder());
    }

    public static class SpaceScopedCloudFoundryOperationsFactory {

        private final DefaultCloudFoundryOperations.Builder defaultCloudFoundryOperationsBuilder;

        public SpaceScopedCloudFoundryOperationsFactory(
            DefaultCloudFoundryOperations.Builder defaultCloudFoundryOperationsBuilder) {
            this.defaultCloudFoundryOperationsBuilder = defaultCloudFoundryOperationsBuilder;
        }

        public CloudFoundryOperations makeCloudFoundryOperations(String spaceName) {
            return defaultCloudFoundryOperationsBuilder
                .space(spaceName)
                .build();
        }
    }

}
