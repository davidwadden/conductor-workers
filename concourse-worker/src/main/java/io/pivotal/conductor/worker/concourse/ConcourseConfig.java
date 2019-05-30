package io.pivotal.conductor.worker.concourse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.conductor.worker.concourse.SetConcoursePipelineWorker.CloudFoundryProperties;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    @Bean
    public RestOperations concourseRestOperations() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(concourseTokenAuthenticationInterceptor()));
        return restTemplate;
    }

    @Bean
    public RestOperations concourseTokenRestOperations() {
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

    @Bean
    public ClientHttpRequestInterceptor concourseTokenAuthenticationInterceptor() {
        return new ConcourseTokenAuthenticationInterceptor(properties, concourseTokenRestOperations());
    }

    public static class ConcourseTokenAuthenticationInterceptor implements
        ClientHttpRequestInterceptor {

        private final ConcourseProperties properties;
        private final RestOperations restOperations;

        public ConcourseTokenAuthenticationInterceptor(ConcourseProperties properties,
            RestOperations restOperations) {
            this.properties = properties;
            this.restOperations = restOperations;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

            String bearerToken = getToken();
            request.getHeaders()
                .add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", bearerToken));

            return execution.execute(request, body);
        }

        private String getToken() {
            String requestUrl = properties.getApiHost() + "/sky/token";

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>() {{
                add("grant_type", "password");
                add("username", properties.getUsername());
                add("password", properties.getPassword());
                add("scope", "openid profile email federated:id groups");
            }};

            String authToken = Base64.getEncoder()
                .encodeToString(("fly" + ":" + "Zmx5").getBytes());
            RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(URI.create(requestUrl))
                .header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", authToken))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData);

            ResponseEntity<TokenResponseDto> responseEntity =
                restOperations.exchange(requestEntity, TokenResponseDto.class);

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                String exceptionMessage = String.format("Acquire token from %s failed: status=%s",
                    requestUrl, responseEntity.getStatusCode());
                throw new RuntimeException(exceptionMessage);
            }

            return responseEntity.getBody().getAccessToken();
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class TokenResponseDto {

            private final String accessToken;

            @JsonCreator
            public TokenResponseDto(@JsonProperty("access_token") String accessToken) {
                this.accessToken = accessToken;
            }

            public String getAccessToken() {
                return accessToken;
            }
        }
    }
}
