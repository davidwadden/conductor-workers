package io.pivotal.conductor.worker.concourse;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ConcourseProperties {

    private String apiHost;
    private String teamName;
    private String username;
    private String password;
    private String shouldExposePipeline;

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
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

    public String getShouldExposePipeline() {
        return shouldExposePipeline;
    }

    public void setShouldExposePipeline(String shouldExposePipeline) {
        this.shouldExposePipeline = shouldExposePipeline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConcourseProperties that = (ConcourseProperties) o;

        return new EqualsBuilder()
            .append(apiHost, that.apiHost)
            .append(teamName, that.teamName)
            .append(username, that.username)
            .append(password, that.password)
            .append(shouldExposePipeline, that.shouldExposePipeline)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(apiHost)
            .append(teamName)
            .append(username)
            .append(password)
            .append(shouldExposePipeline)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("apiHost", apiHost)
            .append("teamName", teamName)
            .append("username", username)
            .append("password", password)
            .append("shouldExposePipeline", shouldExposePipeline)
            .toString();
    }
}
