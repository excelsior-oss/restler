package org.restler.http.security.authorization;

/**
 * AuthorizationStrategy implementations is responsible for obtaining an authentication token
 * Implementations of this interface should be stateless, since they may be reused between different proxies and services.
 */
public interface AuthorizationStrategy {

    /**
     * Provides an authentication token.
     *
     * @return a token object.
     */
    Object authorize();

}
