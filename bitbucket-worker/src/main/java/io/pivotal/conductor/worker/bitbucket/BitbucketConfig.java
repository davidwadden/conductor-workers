package io.pivotal.conductor.worker.bitbucket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BitbucketConfig {

    @Bean
    public RestOperations bitbucketRestOperations() {
        return new RestTemplate();
    }

    @Bean
    public ProjectKeyGenerator bitbucketProjectKeyGenerator() {
        return new RandomProjectKeyGenerator();
    }

}
