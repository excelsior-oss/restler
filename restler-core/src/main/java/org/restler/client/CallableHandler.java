package org.restler.client;

import java.util.concurrent.Callable;

public class CallableHandler extends AbstractWrapperHandler {

    @Override
    protected Class<?> wrapperClass() {
        return Callable.class;
    }

    @Override
    protected Object execute(CallExecutor callExecutor, Call actualCall) {
        return (Callable<Object>) () -> callExecutor.execute(actualCall);
    }

}
