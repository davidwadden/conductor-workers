package io.pivotal.conductor.worker.jira;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class JiraProperties {

    private String username;
    private String password;
    private String apiUrl;
    private String accountId;

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

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JiraProperties that = (JiraProperties) o;

        return new EqualsBuilder()
            .append(username, that.username)
            .append(password, that.password)
            .append(apiUrl, that.apiUrl)
            .append(accountId, that.accountId)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(username)
            .append(password)
            .append(apiUrl)
            .append(accountId)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("username", username)
            .append("password", password)
            .append("apiUrl", apiUrl)
            .append("accountId", accountId)
            .toString();
    }
}
