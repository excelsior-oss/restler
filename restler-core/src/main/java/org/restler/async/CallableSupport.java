package org.restler.async;

import org.restler.client.AbstractWrapperHandler;
import org.restler.client.Call;
import org.restler.client.CallExecutor;

import java.util.concurrent.Callable;

public class CallableSupport extends AbstractWrapperHandler {

    @Override
    protected Class<?> wrapperClass() {
        return Callable.class;
    }

    @Override
    protected Object execute(CallExecutor callExecutor, Call actualCall) {
        return (Callable<Object>) () -> callExecutor.execute(actualCall);
    }

}
