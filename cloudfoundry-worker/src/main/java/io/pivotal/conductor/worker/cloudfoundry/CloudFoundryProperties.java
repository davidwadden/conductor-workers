package io.pivotal.conductor.worker.cloudfoundry;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CloudFoundryProperties {

    private Map<String, CloudFoundryFoundationProperties> foundations = new LinkedHashMap<>();

    public Map<String, CloudFoundryFoundationProperties> getFoundations() {
        return foundations;
    }

    public void setFoundations(
        Map<String, CloudFoundryFoundationProperties> foundations) {
        this.foundations = foundations;
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
            .append(foundations, that.foundations)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(foundations)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("foundations", foundations)
            .toString();
    }

    public static class CloudFoundryFoundationProperties {

        private String apiHost;
        private String appsManagerHost;
        private String username;
        private String password;
        private Boolean skipSslValidation;

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

        public Boolean getSkipSslValidation() {
            return skipSslValidation;
        }

        public void setSkipSslValidation(Boolean skipSslValidation) {
            this.skipSslValidation = skipSslValidation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CloudFoundryFoundationProperties that = (CloudFoundryFoundationProperties) o;

            return new EqualsBuilder()
                .append(apiHost, that.apiHost)
                .append(appsManagerHost, that.appsManagerHost)
                .append(username, that.username)
                .append(password, that.password)
                .append(skipSslValidation, that.skipSslValidation)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(apiHost)
                .append(appsManagerHost)
                .append(username)
                .append(password)
                .append(skipSslValidation)
                .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("apiHost", apiHost)
                .append("appsManagerHost", appsManagerHost)
                .append("username", username)
                .append("password", password)
                .append("skipSslValidation", skipSslValidation)
                .toString();
        }

    }
}
