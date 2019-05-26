package io.pivotal.conductor.worker.bitbucket;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class BitbucketProperties {

    private String username;
    private String password;
    private String teamName;

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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BitbucketProperties that = (BitbucketProperties) o;

        return new EqualsBuilder()
            .append(username, that.username)
            .append(password, that.password)
            .append(teamName, that.teamName)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(username)
            .append(password)
            .append(teamName)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("username", username)
            .append("password", password)
            .append("teamName", teamName)
            .toString();
    }
}
