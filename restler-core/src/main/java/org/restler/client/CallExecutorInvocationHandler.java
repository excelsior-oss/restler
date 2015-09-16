package org.restler.client;

import net.sf.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

public class CallExecutorInvocationHandler implements InvocationHandler {

    private final CallExecutor callExecutor;
    private final BiFunction<Method, Object[], Call> mapToCall;

    public CallExecutorInvocationHandler(CallExecutor callExecutor, BiFunction<Method, Object[], Call> mapToCall) {
        this.callExecutor = callExecutor;
        this.mapToCall = mapToCall;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        Call call = mapToCall.apply(method, args);
        return callExecutor.execute(call);
    }
}
