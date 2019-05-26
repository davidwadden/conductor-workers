package io.pivotal.conductor.worker.cloudfoundry;

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

    @Autowired
    private CloudFoundryProperties properties;

    @Bean
    public ConnectionContext connectionContext() {
        return DefaultConnectionContext.builder()
            .apiHost(properties.getApiHost())
            .build();
    }

    @Bean
    public TokenProvider tokenProvider() {
        return PasswordGrantTokenProvider.builder()
            .password(properties.getPassword())
            .username(properties.getUsername())
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
    public CloudFoundryOperations cloudFoundryOperations() {
        return DefaultCloudFoundryOperations.builder()
            .cloudFoundryClient(cloudFoundryClient())
            .dopplerClient(dopplerClient())
            .uaaClient(uaaClient())
            .organization(properties.getOrganization())
            .build();
    }

}
