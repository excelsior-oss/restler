package org.restler.spring.data.calls;

import org.restler.client.CallExecutor;
import org.restler.spring.data.methods.SaveRepositoryMethod;

public class LazyCallEnhancer extends CustomCallEnhancer<SaveRepositoryMethod.LazyCall> {
    public LazyCallEnhancer() {
        super(SaveRepositoryMethod.LazyCall.class);
    }

    @Override
    protected Object enhance(SaveRepositoryMethod.LazyCall call, CallExecutor callExecutor) {
        return callExecutor.execute(call.getCall());
    }
}
