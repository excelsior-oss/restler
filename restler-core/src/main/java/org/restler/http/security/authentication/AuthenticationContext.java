package org.restler.http.security.authentication;

/**
 * Implementations provide context for authentication.
 */
public interface AuthenticationContext {

    /**
     * Returns the strategy used with the context.
     * @return a strategy object
     */
    AuthenticationStrategy getAuthenticationStrategy();

    /**
     * Returns the last authentication token assigned to the context.
     * @return a token
     */
    Object getAuthenticationToken();

    /**
     * Assigns an authentication token to the context to be later used by the strategy.
     * @param token any token compatible with the authentication strategy.
     */
    void setAuthenticationToken(Object token);
}
