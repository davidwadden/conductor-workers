package io.pivotal.conductor.worker.cloudfoundry;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CloudFoundryProperties {

    private String apiHost;
    private String appsManagerHost;
    private String organization;
    private String username;
    private String password;
    private String domain;
    private String logDrainUrl;

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getAppsManagerHost() {
        return appsManagerHost;
    }

    public void setAppsManagerHost(String appsManagerHost) {
        this.appsManagerHost = appsManagerHost;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLogDrainUrl() {
        return logDrainUrl;
    }

    public void setLogDrainUrl(String logDrainUrl) {
        this.logDrainUrl = logDrainUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CloudFoundryProperties that = (CloudFoundryProperties) o;

        return new EqualsBuilder()
            .append(apiHost, that.apiHost)
            .append(appsManagerHost, that.appsManagerHost)
            .append(organization, that.organization)
            .append(username, that.username)
            .append(password, that.password)
            .append(domain, that.domain)
            .append(logDrainUrl, that.logDrainUrl)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(apiHost)
            .append(appsManagerHost)
            .append(organization)
            .append(username)
            .append(password)
            .append(domain)
            .append(logDrainUrl)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("apiHost", apiHost)
            .append("appsManagerHost", appsManagerHost)
            .append("organization", organization)
            .append("username", username)
            .append("password", password)
            .append("domain", domain)
            .append("logDrainUrl", logDrainUrl)
            .toString();
    }
}
