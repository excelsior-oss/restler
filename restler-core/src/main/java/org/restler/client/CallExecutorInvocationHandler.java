package org.restler.client;

import net.sf.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

/**
 * Simple implemenation of {@code InvocationHandler} that connects provided {@code MethodInvocationMapper} and
 * {@code CallExecutor}. Core modules are encouraged to produce instances of this class parametrized with custom
 * call executor and method invocation mapper.
 *
 * @see CoreModule
 */
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
