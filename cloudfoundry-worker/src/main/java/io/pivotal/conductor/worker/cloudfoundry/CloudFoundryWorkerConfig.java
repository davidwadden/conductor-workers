package io.pivotal.conductor.worker.cloudfoundry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudFoundryWorkerConfig {

    @Bean
    public CreateCloudFoundrySpaceWorker createCloudFoundrySpaceWorker() {
        return new CreateCloudFoundrySpaceWorker();
    }

    @Bean
    public CreateCloudFoundryRouteWorker createCloudFoundryRouteWorker() {
        return new CreateCloudFoundryRouteWorker();
    }

}
