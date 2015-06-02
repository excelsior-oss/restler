package org.restler;

import org.restler.http.security.authentication.AuthenticationContext;
import org.restler.http.security.authorization.AuthorizationContext;
import org.restler.http.security.authorization.AuthorizationStrategy;

/**
 * Description of a service.
 */
class Session implements AuthorizationContext, AuthenticationContext {

    private AuthorizationStrategy authorizationStrategy;
    private Object authenticationToken;

    public Session(AuthorizationStrategy authorizationStrategy) {
        this.authorizationStrategy = authorizationStrategy;
    }

    @Override
    public Object getAuthenticationToken() {
        return authenticationToken;
    }

    @Override
    public void setAuthenticationToken(Object authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    @Override
    public AuthorizationStrategy getAuthorizationStrategy() {
        return authorizationStrategy;
    }
}
