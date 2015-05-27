package org.restler.http.security.authentication;

import org.restler.http.ExecutableRequest;
import org.springframework.http.ResponseEntity;

/**
 * Created by pasa on 19.02.2015.
 */
public interface AuthenticationStrategy {

    default void setAuthenticationToken(Object token){}

    default <T> ResponseEntity<T> executeAuthenticatedRequest(ExecutableRequest<T> request) {
        return request.execute();
    }
}
