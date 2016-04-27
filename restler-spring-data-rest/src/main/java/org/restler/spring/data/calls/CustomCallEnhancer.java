package org.restler.spring.data.calls;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;

public abstract class CustomCallEnhancer<T extends Call> implements CallEnhancer {

    private final Class<T> callClass;

    public CustomCallEnhancer(Class<T> callClass) {
        this.callClass = callClass;
    }

    public Object apply(Call call, CallExecutor callExecutor) {
        if (callClass.equals(call.getClass())) {
            return enhance((T) call, callExecutor);
        } else {
            return callExecutor.execute(call);
        }
    }

    protected abstract Object enhance(T call, CallExecutor callExecutor);

}
