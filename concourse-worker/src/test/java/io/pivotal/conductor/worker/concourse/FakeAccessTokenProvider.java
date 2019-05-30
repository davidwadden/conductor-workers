package io.pivotal.conductor.worker.concourse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserApprovalRequiredException;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

class FakeAccessTokenProvider extends ResourceOwnerPasswordAccessTokenProvider {

    @Override
    public OAuth2AccessToken obtainAccessToken(OAuth2ProtectedResourceDetails details,
        AccessTokenRequest parameters)
        throws UserRedirectRequiredException, UserApprovalRequiredException, AccessDeniedException {

        return new DefaultOAuth2AccessToken("some-access-token");
    }
}
