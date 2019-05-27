package io.pivotal.conductor.worker.tracker;

import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TrackerConfig {

    @Autowired
    private TrackerProperties properties;

    @Bean
    public RestOperations trackerRestOperations(
        ClientHttpRequestInterceptor trackerTokenAuthenticationInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(trackerTokenAuthenticationInterceptor));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestInterceptor trackerTokenAuthenticationInterceptor() {
        return new TrackerTokenAuthenticationInterceptor(properties);
    }

    public static class TrackerTokenAuthenticationInterceptor implements
        ClientHttpRequestInterceptor {

        private final TrackerProperties properties;

        public TrackerTokenAuthenticationInterceptor(TrackerProperties properties) {
            this.properties = properties;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

            request.getHeaders().add("X-TrackerToken", properties.getApiKey());

            return execution.execute(request, body);
        }
    }

}
