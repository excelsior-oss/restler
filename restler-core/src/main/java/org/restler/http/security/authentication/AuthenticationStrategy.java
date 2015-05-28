package org.restler.http.security.authentication;

import org.restler.http.ExecutableRequest;
import org.springframework.http.ResponseEntity;

/**
 * AuthenticationStrategy implementations makes {@link ExecutableRequest} execution authorized by authenticating the request. Together with {@link org.restler.http.security.authorization.AuthorizationStrategy}, it is used to maintain a secured session.
 */
public interface AuthenticationStrategy {

    /**
     * Sets an authentication token that will be used for authenticating requests. Usually, a token is provided by {@link org.restler.http.security.authorization.AuthorizationStrategy}.
     * @param token a token object.
     * @throws IllegalArgumentException if the token type is not supported by the implementation.
     */
    default void setAuthenticationToken(Object token) throws IllegalArgumentException {}

    /**
     * Authenticates and executes the request.
     * @param request the request to be authenticated and executed
     * @param <T> expected type of the response
     * @return the result of the request execution
     */
    default <T> ResponseEntity<T> executeAuthenticatedRequest(ExecutableRequest<T> request) {
        return request.execute();
    }
}
