package org.restler.http.security;

import org.restler.http.security.authentication.AuthenticationContext;
import org.restler.http.security.authentication.AuthenticationStrategy;
import org.restler.http.security.authorization.AuthorizationStrategy;

/**
 * Maintains security aspect of a user session
 */
public class SecuritySession implements AuthenticationContext {

    private final AuthorizationStrategy authorizationStrategy;
    private final AuthenticationStrategy authenticationStrategy;
    private final boolean autoAuthorize;

    private Object authenticationToken;
    private boolean authorized;

    public SecuritySession(AuthorizationStrategy authorizationStrategy, AuthenticationStrategy authenticationStrategy, boolean autoAuthorize) {
        this.authenticationStrategy = authenticationStrategy;
        this.authorizationStrategy = authorizationStrategy;
        this.autoAuthorize = autoAuthorize;
    }

    @Override
    public Object getAuthenticationToken() {
        if (authorized) {
            return authenticationToken;
        } else {
            if (autoAuthorize) {
                authorize();
                return authenticationToken;
            } else {
                throw new IllegalStateException("Unauthorized session can't provide authentication token.");
            }
        }

    }

    public void authorize() {
        if (authorizationStrategy == null) {
            throw new IllegalStateException("Can't authorize session as authorization strategy is not set.");
        }

        authenticationToken = authorizationStrategy.authorize();
        authorized = true;
    }

    public AuthenticationStrategy getAuthenticationStrategy() {
        return authenticationStrategy;
    }

}
