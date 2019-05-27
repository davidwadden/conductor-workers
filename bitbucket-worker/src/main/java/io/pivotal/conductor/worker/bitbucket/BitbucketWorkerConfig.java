package io.pivotal.conductor.worker.bitbucket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestOperations;

@Import(BitbucketConfig.class)
@Configuration
public class BitbucketWorkerConfig {

    @Autowired
    private BitbucketProperties properties;

    @Bean
    public CreateBitbucketProjectWorker createBitbucketProjectWorker(
        RestOperations bitbucketRestOperations,
        ProjectKeyGenerator bitbucketProjectKeyGenerator) {
        return new CreateBitbucketProjectWorker(properties, bitbucketRestOperations, bitbucketProjectKeyGenerator);
    }

    @Bean
    public DeleteBitbucketProjectWorker deleteBitbucketProjectWorker(
        RestOperations bitbucketRestOperations) {
        return new DeleteBitbucketProjectWorker(properties, bitbucketRestOperations);
    }

}
