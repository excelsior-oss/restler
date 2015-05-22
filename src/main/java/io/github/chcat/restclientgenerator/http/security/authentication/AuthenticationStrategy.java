package io.github.chcat.restclientgenerator.http.security.authentication;

import io.github.chcat.restclientgenerator.http.RequestExecutor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

/**
 * Created by pasa on 19.02.2015.
 */
public interface AuthenticationStrategy {

    default void setAuthenticationToken(Object token){}

    default <T> ResponseEntity<T> executeAuthenticatedRequest(RequestExecutor executor, RequestEntity<?> request, Class<T> responseType) {
        return executor.execute(request,responseType);
    }

}
