package io.pivotal.conductor.worker.cloudfoundry;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.uaa.UaaClient;

public class CloudFoundryClients {

    private final ConnectionContext connectionContext;
    private final TokenProvider tokenProvider;
    private final CloudFoundryClient cloudFoundryClient;
    private final DopplerClient dopplerClient;
    private final UaaClient uaaClient;
    private final DefaultCloudFoundryOperations.Builder cloudFoundryOperationsBuilder;
    private final CloudFoundryOperations cloudFoundryOperations;

    public CloudFoundryClients(ConnectionContext connectionContext,
        TokenProvider tokenProvider, CloudFoundryClient cloudFoundryClient,
        DopplerClient dopplerClient, UaaClient uaaClient,
        DefaultCloudFoundryOperations.Builder cloudFoundryOperationsBuilder,
        CloudFoundryOperations cloudFoundryOperations) {
        this.connectionContext = connectionContext;
        this.tokenProvider = tokenProvider;
        this.cloudFoundryClient = cloudFoundryClient;
        this.dopplerClient = dopplerClient;
        this.uaaClient = uaaClient;
        this.cloudFoundryOperationsBuilder = cloudFoundryOperationsBuilder;
        this.cloudFoundryOperations = cloudFoundryOperations;
    }

    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    public TokenProvider getTokenProvider() {
        return tokenProvider;
    }

    public CloudFoundryClient getCloudFoundryClient() {
        return cloudFoundryClient;
    }

    public DopplerClient getDopplerClient() {
        return dopplerClient;
    }

    public UaaClient getUaaClient() {
        return uaaClient;
    }

    public DefaultCloudFoundryOperations.Builder getCloudFoundryOperationsBuilder() {
        return cloudFoundryOperationsBuilder;
    }

    public CloudFoundryOperations getCloudFoundryOperations() {
        return cloudFoundryOperations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CloudFoundryClients that = (CloudFoundryClients) o;

        return new EqualsBuilder()
            .append(connectionContext, that.connectionContext)
            .append(tokenProvider, that.tokenProvider)
            .append(cloudFoundryClient, that.cloudFoundryClient)
            .append(dopplerClient, that.dopplerClient)
            .append(uaaClient, that.uaaClient)
            .append(cloudFoundryOperationsBuilder, that.cloudFoundryOperationsBuilder)
            .append(cloudFoundryOperations, that.cloudFoundryOperations)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(connectionContext)
            .append(tokenProvider)
            .append(cloudFoundryClient)
            .append(dopplerClient)
            .append(uaaClient)
            .append(cloudFoundryOperationsBuilder)
            .append(cloudFoundryOperations)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("connectionContext", connectionContext)
            .append("tokenProvider", tokenProvider)
            .append("cloudFoundryClient", cloudFoundryClient)
            .append("dopplerClient", dopplerClient)
            .append("uaaClient", uaaClient)
            .append("cloudFoundryOperationsBuilder", cloudFoundryOperationsBuilder)
            .append("cloudFoundryOperations", cloudFoundryOperations)
            .toString();
    }
}
