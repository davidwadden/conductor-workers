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
    public CreateCloudFoundryOrganizationWorker createCloudFoundryOrganizationWorker(
        CloudFoundryOrganizationClient cloudFoundryOrganizationClient) {
        return new CreateCloudFoundryOrganizationWorker(cloudFoundryOrganizationClient);
    }

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
    public CreateCloudFoundryServiceInstanceWorker createCloudFoundryServiceInstanceWorker(
        CloudFoundryServiceClient cloudFoundryServiceInstanceClient) {
        return new CreateCloudFoundryServiceInstanceWorker(cloudFoundryServiceInstanceClient);
    }

    @Bean
    public DeleteCloudFoundryOrganizationWorker deleteCloudFoundryOrganizationWorker(
        CloudFoundryOrganizationClient cloudFoundryOrganizationClient) {
        return new DeleteCloudFoundryOrganizationWorker(cloudFoundryOrganizationClient);
    }

    @Bean
    public DeleteCloudFoundrySpaceWorker deleteCloudFoundrySpaceWorker(
        CloudFoundrySpaceClient cloudFoundrySpaceClient) {
        return new DeleteCloudFoundrySpaceWorker(cloudFoundrySpaceClient);
    }

    @Bean
    public DeleteCloudFoundryServiceInstanceWorker deleteCloudFoundryServiceInstanceWorker(
        CloudFoundryServiceClient cloudFoundryServiceInstanceClient) {
        return new DeleteCloudFoundryServiceInstanceWorker(cloudFoundryServiceInstanceClient);
    }

}
