package org.restler.http.security.authorization;

/**
 * Implementations provide context for authentication.
 */
public interface AuthorizationContext {

    /**
     * Assigns an authentication token to the context to be later used by the strategy.
     */
    void retrieveAuthenticationToken();

}
