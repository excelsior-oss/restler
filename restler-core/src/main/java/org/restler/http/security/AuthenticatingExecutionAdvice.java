package org.restler.http.security;

import org.restler.http.Request;
import org.restler.http.RequestExecutionAdvice;
import org.restler.http.RequestExecutor;
import org.restler.http.Response;

public class AuthenticatingExecutionAdvice implements RequestExecutionAdvice {

    private final SecuritySession session;

    public AuthenticatingExecutionAdvice(SecuritySession session) {
        this.session = session;
    }

    @Override
    public <T> Response<T> advice(Request<T> request, RequestExecutor requestExecutor) {
        Request<T> authenticatedRequest = session.getAuthenticationStrategy().authenticate(request, session);
        return requestExecutor.execute(authenticatedRequest);
    }
}
