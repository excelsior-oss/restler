package org.restler.spring.data;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;
import org.restler.http.HttpExecutionException;
import org.springframework.http.HttpStatus;

public class HttpExceptionCallEnhancer implements CallEnhancer {
    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        try {
            return callExecutor.execute(call);
        } catch(HttpExecutionException e) {
            if(e.getStatus().code == HttpStatus.NOT_FOUND.value() && e.getStatus().line.equals(HttpStatus.NOT_FOUND.getReasonPhrase())) {
                return null;
            }

            throw e;
        }
    }
}
