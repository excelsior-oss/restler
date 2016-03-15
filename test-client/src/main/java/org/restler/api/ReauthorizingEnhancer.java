package org.restler.api;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;
import org.restler.http.HttpExecutionException;
import org.restler.http.security.SecuritySession;

public class ReauthorizingEnhancer implements CallEnhancer {

    private final SecuritySession session;

    public ReauthorizingEnhancer(SecuritySession session) {
        this.session = session;
    }

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        int httpStatusForbidden = 403;
        try {
            return callExecutor.execute(call);
        } catch (HttpExecutionException e) {
            if (e.getStatus().code == httpStatusForbidden) {
                session.authorize();
                return callExecutor.execute(call);
            } else {
                throw e;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

}
