package io.pivotal.conductor.worker.bitbucket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(BitbucketConfig.class)
@Configuration
public class BitbucketWorkerConfig {

    @Autowired
    private BitbucketProperties properties;

}
