package org.restler.http.security;

import org.restler.client.HttpCall;
import org.restler.http.RequestExecutionAdvice;
import org.restler.http.RequestExecutor;
import org.restler.http.Response;

public class AuthenticatingExecutionAdvice implements RequestExecutionAdvice {

    private final SecuritySession session;

    public AuthenticatingExecutionAdvice(SecuritySession session) {
        this.session = session;
    }

    @Override
    public <T> Response<T> advice(HttpCall<T> call, RequestExecutor requestExecutor) {
        HttpCall<T> authenticatedRequest = session.getAuthenticationStrategy().authenticate(call, session);
        return requestExecutor.execute(authenticatedRequest);
    }
}
