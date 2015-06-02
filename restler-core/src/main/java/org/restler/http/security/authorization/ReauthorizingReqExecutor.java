package org.restler.http.security.authorization;

import org.restler.http.ExecutableRequest;
import org.restler.http.HttpRequestExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public class ReauthorizingReqExecutor implements HttpRequestExecutor {

    private final HttpRequestExecutor delegate;
    private final AuthorizationContext context;
    private final AuthorizationStrategy authorizationStrategy;

    public ReauthorizingReqExecutor(HttpRequestExecutor delegate, AuthorizationContext context, AuthorizationStrategy authorizationStrategy) {
        this.delegate = delegate;
        this.context = context;
        this.authorizationStrategy = authorizationStrategy;
    }

    @Override
    public <T> ResponseEntity<T> execute(ExecutableRequest<T> executableRequest) {
        ResponseEntity<T> response;
        try {
            response = delegate.execute(executableRequest);
        } catch (HttpClientErrorException e) {
            response = new ResponseEntity<T>(HttpStatus.FORBIDDEN);
        }

        if (response.getStatusCode().value() == HttpStatus.FORBIDDEN.value()) {
            context.setAuthenticationToken(authorizationStrategy.authorize());
            return delegate.execute(executableRequest);
        }

        return response;
    }
}
