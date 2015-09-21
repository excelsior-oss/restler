package org.restler.client;

import net.sf.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

public class CallExecutorInvocationHandler implements InvocationHandler {

    private final CallExecutor callExecutor;
    private final MethodInvocationMapper mapToCall;

    public CallExecutorInvocationHandler(CallExecutor callExecutor, MethodInvocationMapper mapToCall) {
        this.callExecutor = callExecutor;
        this.mapToCall = mapToCall;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        Call call = mapToCall.map(o, method, args);
        return callExecutor.execute(call);
    }
}
