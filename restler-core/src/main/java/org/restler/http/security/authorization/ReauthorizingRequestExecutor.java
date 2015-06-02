package org.restler.http.security.authorization;

import org.restler.http.ExecutableRequest;
import org.restler.http.HttpRequestExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public class ReauthorizingRequestExecutor implements HttpRequestExecutor {

    private final HttpRequestExecutor delegate;
    private final AuthorizationContext context;

    public ReauthorizingRequestExecutor(HttpRequestExecutor delegate, AuthorizationContext context) {
        this.delegate = delegate;
        this.context = context;
    }

    @Override
    public <T> ResponseEntity<T> execute(ExecutableRequest<T> executableRequest) {
        ResponseEntity<T> response;
        try {
            response = delegate.execute(executableRequest);
        } catch (HttpClientErrorException e) {
            response = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (response.getStatusCode().value() == HttpStatus.FORBIDDEN.value()) {
            context.retrieveAuthenticationToken();
            return delegate.execute(executableRequest);
        }

        return response;
    }
}
