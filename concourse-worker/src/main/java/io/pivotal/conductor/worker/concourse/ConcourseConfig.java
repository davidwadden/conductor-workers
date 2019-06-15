package io.pivotal.conductor.worker.concourse;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;

@Configuration
public class ConcourseConfig {

    @Autowired
    private ConcourseProperties properties;

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
        resourceDetails.setScope(ImmutableList.of("openid", "profile", "email", "federated:id", "groups"));
        return resourceDetails;
    }
}
