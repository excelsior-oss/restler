package org.restler.http.security.authentication;

import org.restler.http.ExecutableRequest;
import org.springframework.http.ResponseEntity;

/**
 * AuthenticationStrategy implementations makes {@link ExecutableRequest} execution authorized by authenticating the request. Together with {@link org.restler.http.security.authorization.AuthorizationStrategy}, it is used to maintain a secured session.
 */
public interface AuthenticationStrategy {

    /**
     * Authenticates and executes the request.
     * @param request the request to be authenticated and executed
     * @param <T> expected type of the response
     * @param context is a context of authentication process
     * @return the result of the request execution
     */
    default <T> ResponseEntity<T> authenticatedAndExecute(ExecutableRequest<T> request, AuthenticationContext context) {
        return request.execute();
    }
}
