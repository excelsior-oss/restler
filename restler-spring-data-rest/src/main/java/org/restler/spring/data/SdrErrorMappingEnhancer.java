package org.restler.spring.data;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;
import org.restler.http.HttpExecutionException;
import org.springframework.http.HttpStatus;

public class SdrErrorMappingEnhancer implements CallEnhancer {
    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        try {
            return callExecutor.execute(call);
        } catch(HttpExecutionException e) {
            //if repository or resource wasn't found
            if(equalsStatus(e.getStatus(), HttpStatus.NOT_FOUND)) {
                return null;
            }
            //if one of standard methods was not exported or if was tried unbind the association that non-optional
            else if(equalsStatus(e.getStatus(), HttpStatus.METHOD_NOT_ALLOWED)) {
                return null;
            }

            throw e;
        }
    }

    private boolean equalsStatus(org.restler.http.HttpStatus restlerStatus, HttpStatus springStatus) {
        return restlerStatus.code == springStatus.value() && restlerStatus.line.equals(springStatus.getReasonPhrase());
    }
}
