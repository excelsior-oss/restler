package org.restler.http.security;

import org.restler.client.Call;
import org.restler.client.CallExecutionAdvice;
import org.restler.client.CallExecutor;

public class AuthenticatingExecutionAdvice implements CallExecutionAdvice<Object> {

    private final SecuritySession session;

    public AuthenticatingExecutionAdvice(SecuritySession session) {
        this.session = session;
    }

    @Override
    public Object advice(Call call, CallExecutor requestExecutor) {
        Call authenticatedRequest = session.getAuthenticationStrategy().authenticate(call, session);
        return requestExecutor.execute(authenticatedRequest);
    }
}
