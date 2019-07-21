package io.pivotal.conductor.worker.app;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("portal.conductor")
public class ConductorProperties {

    private String conductorRootUri;
    private Float threadsPerWorker;

    public String getConductorRootUri() {
        return conductorRootUri;
    }

    public void setConductorRootUri(String conductorRootUri) {
        this.conductorRootUri = conductorRootUri;
    }

    public Float getThreadsPerWorker() {
        return threadsPerWorker;
    }

    public void setThreadsPerWorker(Float threadsPerWorker) {
        this.threadsPerWorker = threadsPerWorker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConductorProperties that = (ConductorProperties) o;

        return new EqualsBuilder()
            .append(conductorRootUri, that.conductorRootUri)
            .append(threadsPerWorker, that.threadsPerWorker)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(conductorRootUri)
            .append(threadsPerWorker)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("conductorRootUri", conductorRootUri)
            .append("threadsPerWorker", threadsPerWorker)
            .toString();
    }
}
