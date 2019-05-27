package io.pivotal.conductor.worker.bitbucket;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BitbucketConfig {

    @Autowired
    private BitbucketProperties properties;

    @Bean
    public RestOperations bitbucketRestOperations(
        ClientHttpRequestInterceptor bitbucketBasicAuthenticationInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(bitbucketBasicAuthenticationInterceptor));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestInterceptor bitbucketBasicAuthenticationInterceptor() {
        return new BasicAuthenticationInterceptor(
            properties.getUsername(), properties.getPassword());
    }

    @Bean
    public ProjectKeyGenerator bitbucketProjectKeyGenerator() {
        return new RandomProjectKeyGenerator();
    }

}
