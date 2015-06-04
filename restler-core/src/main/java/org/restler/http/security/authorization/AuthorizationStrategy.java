package org.restler.http.security.authorization;

/**
 * AuthorizationStrategy implementations a responsible for obtaining an authentication token
 */
public interface AuthorizationStrategy {

    /**
     * Provides an authentication token.
     *
     * @return a token object.
     */
    Object authorize();

}
