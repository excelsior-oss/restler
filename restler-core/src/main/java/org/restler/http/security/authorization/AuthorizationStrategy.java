package org.restler.http.security.authorization;

/**
 * AuthorizationStrategy implementations a responsible for obtaining an authentication token later used by {@link org.restler.http.security.authentication.AuthenticationStrategy} to support secured sessions.
 */
public interface AuthorizationStrategy {

    /**
     * Provides an authentication token.
     *
     * @return a token object.
     */
    Object authorize();

}
