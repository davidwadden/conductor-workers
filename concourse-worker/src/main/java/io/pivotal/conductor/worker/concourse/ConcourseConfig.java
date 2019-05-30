package io.pivotal.conductor.worker.concourse;

import io.pivotal.conductor.worker.concourse.SetConcoursePipelineWorker.CloudFoundryProperties;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConcourseConfig {

    @Autowired
    private ConcourseProperties properties;

    @Bean
    public CloudFoundryProperties cloudFoundryProperties() {
        CloudFoundryProperties properties = new CloudFoundryProperties();
        properties.setUsername("some-cf-username");
        properties.setUsername("some-cf-password");
        return properties;
    }

    @Deprecated
    @Bean
    public RestOperations concourseRestOperations() {
        return new RestTemplate();
    }

    @Bean
    public OAuth2RestOperations concourseOAuth2RestOperations() {
        return new OAuth2RestTemplate(concourseOAuth2ProtectedResourceDetails());
    }

    @Bean
    public OAuth2ProtectedResourceDetails concourseOAuth2ProtectedResourceDetails() {
        ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        resourceDetails.setAccessTokenUri(properties.getApiHost() + "/sky/token");
        resourceDetails.setUsername(properties.getUsername());
        resourceDetails.setPassword(properties.getPassword());
        resourceDetails.setClientId("fly");
        resourceDetails.setClientSecret("Zmx5");
        resourceDetails.setScope(List.of("openid", "profile", "email", "federated:id", "groups"));
        return resourceDetails;
    }
}
