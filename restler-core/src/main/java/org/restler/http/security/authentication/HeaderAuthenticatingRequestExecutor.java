package org.restler.http.security.authentication;

import org.restler.http.ExecutableRequest;
import org.restler.http.HttpRequestExecutor;
import org.springframework.http.ResponseEntity;

public abstract class HeaderAuthenticatingRequestExecutor implements HttpRequestExecutor {
    private HttpRequestExecutor executor;
    protected AuthenticationContext context;

    protected HeaderAuthenticatingRequestExecutor(HttpRequestExecutor executor, AuthenticationContext context) {
        this.executor = executor;
        this.context = context;
    }

    protected abstract String getHeaderName();

    protected abstract String getHeaderValue();

    @Override
    public <T> ResponseEntity<T> execute(ExecutableRequest<T> executableRequest) {
        ExecutableRequest<T> authenticatedRequest = executableRequest.setHeader(getHeaderName(), getHeaderValue());
        return executor.execute(authenticatedRequest);
    }
}
