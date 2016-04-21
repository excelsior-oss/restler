package org.restler.api;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;
import org.restler.http.HttpExecutionException;
import org.restler.http.security.SecuritySession;

import java.net.HttpURLConnection;

public class ReauthorizingEnhancer implements CallEnhancer {

    private final SecuritySession session;

    public ReauthorizingEnhancer(SecuritySession session) {
        this.session = session;
    }

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        try {
            return callExecutor.execute(call);
        } catch (HttpExecutionException e) {
            if (e.getStatus().code == HttpURLConnection.HTTP_FORBIDDEN) {
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
