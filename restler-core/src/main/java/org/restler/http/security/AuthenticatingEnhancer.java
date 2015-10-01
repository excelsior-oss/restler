package org.restler.http.security;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;

public class AuthenticatingEnhancer implements CallEnhancer {

    private final SecuritySession session;

    public AuthenticatingEnhancer(SecuritySession session) {
        this.session = session;
    }

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        Call authenticatedRequest = session.getAuthenticationStrategy().authenticate(call, session);
        return callExecutor.execute(authenticatedRequest);
    }
}
