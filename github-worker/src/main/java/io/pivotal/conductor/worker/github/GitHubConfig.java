package io.pivotal.conductor.worker.github;

import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GitHubConfig {

    @Autowired
    private GitHubProperties properties;

    @Bean
    public RestOperations gitHubRestOperations(
        ClientHttpRequestInterceptor gitHubTokenAuthenticationInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate
            .setInterceptors(Collections.singletonList(gitHubTokenAuthenticationInterceptor));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestInterceptor gitHubTokenAuthenticationInterceptor() {
        return new GitHubTokenAuthenticationInterceptor(properties);
    }

    public static class GitHubTokenAuthenticationInterceptor implements
        ClientHttpRequestInterceptor {

        private final GitHubProperties properties;

        public GitHubTokenAuthenticationInterceptor(GitHubProperties properties) {
            this.properties = properties;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

            request.getHeaders()
                .add(HttpHeaders.AUTHORIZATION, String.format("token %s", properties.getToken()));

            return execution.execute(request, body);
        }
    }

}
