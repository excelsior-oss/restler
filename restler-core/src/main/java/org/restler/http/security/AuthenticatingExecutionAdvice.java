package org.restler.http.security;

import org.restler.http.ExecutionAdvice;
import org.restler.http.Executor;
import org.restler.http.Request;
import org.springframework.http.ResponseEntity;

public class AuthenticatingExecutionAdvice implements ExecutionAdvice {

    private final SecuritySession session;

    public AuthenticatingExecutionAdvice(SecuritySession session) {
        this.session = session;
    }

    @Override
    public <T> ResponseEntity<T> advice(Request<T> request, Executor executor) {
        Request<T> authenticatedRequest = session.getAuthenticationStrategy().authenticate(request, session);
        return executor.execute(authenticatedRequest);
    }
}
