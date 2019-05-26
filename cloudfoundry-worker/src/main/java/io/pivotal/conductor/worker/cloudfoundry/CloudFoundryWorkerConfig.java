package io.pivotal.conductor.worker.cloudfoundry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(CloudFoundryConfig.class)
@Configuration
public class CloudFoundryWorkerConfig {

    @Bean
    public CreateCloudFoundrySpaceWorker createCloudFoundrySpaceWorker(
        CloudFoundrySpaceClient cloudFoundrySpaceClient) {
        return new CreateCloudFoundrySpaceWorker(cloudFoundrySpaceClient);
    }

    @Bean
    public CreateCloudFoundryRouteWorker createCloudFoundryRouteWorker(
        CloudFoundryProperties properties,
        CloudFoundryRouteClient cloudFoundryRouteClient) {
        return new CreateCloudFoundryRouteWorker(properties, cloudFoundryRouteClient);
    }

    @Bean
    public CreateMysqlDatabaseServiceWorker createMysqlDatabaseServiceWorker(
        CloudFoundryServiceClient cloudFoundryServiceClient) {
        return new CreateMysqlDatabaseServiceWorker(cloudFoundryServiceClient);
    }

    @Bean
    public CreateRabbitMqServiceWorker createRabbitMqServiceWorker() {
        return new CreateRabbitMqServiceWorker();
    }

    @Bean
    public DeleteCloudFoundrySpaceWorker deleteCloudFoundrySpaceWorker(
        CloudFoundrySpaceClient cloudFoundrySpaceClient) {
        return new DeleteCloudFoundrySpaceWorker(cloudFoundrySpaceClient);
    }

}
