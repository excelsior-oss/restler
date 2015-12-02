package org.restler.http.security.authentication;

/**
 * Interface of session state holder, that passed to implementations of AuthenticationStrategy
 */
public interface AuthenticationContext {

    /**
     * Returns the last authentication token assigned to the context.
     *
     * @return a token
     */
    Object getAuthenticationToken();

}
