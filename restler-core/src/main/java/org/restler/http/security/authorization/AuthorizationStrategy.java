package org.restler.http.security.authorization;

/**
 * An AuthorizationStrategy implementation is responsible for obtaining an authentication token
 * Implementations of this interface should be stateless, since they may be reused among different proxies and services.
 */
public interface AuthorizationStrategy {

    /**
     * Provides an authentication token.
     *
     * @return a token object.
     */
    Object authorize();

}
