package io.pivotal.conductor.worker.cloudfoundry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(CloudFoundryConfig.class)
@Configuration
public class CloudFoundryWorkerConfig {

    @Autowired
    private CloudFoundryProperties properties;

    @Bean
    public DeriveCloudFoundryResourceNameWorker deriveCloudFoundryResourceNameWorker() {
        return new DeriveCloudFoundryResourceNameWorker();
    }

    @Bean
    public CreateCloudFoundrySpaceWorker createCloudFoundrySpaceWorker(
        CloudFoundrySpaceClient cloudFoundrySpaceClient) {
        return new CreateCloudFoundrySpaceWorker(properties, cloudFoundrySpaceClient);
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
        return new CreateMysqlDatabaseServiceWorker(properties, cloudFoundryServiceClient);
    }

    @Bean
    public CreateRabbitMqServiceWorker createRabbitMqServiceWorker(
        CloudFoundryServiceClient cloudFoundryServiceClient) {
        return new CreateRabbitMqServiceWorker(properties, cloudFoundryServiceClient);
    }

    @Bean
    public DeleteCloudFoundrySpaceWorker deleteCloudFoundrySpaceWorker(
        CloudFoundrySpaceClient cloudFoundrySpaceClient) {
        return new DeleteCloudFoundrySpaceWorker(properties, cloudFoundrySpaceClient);
    }

}
