package org.restler.client;

import org.restler.http.Executor;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class CallableResultCase implements Function<ServiceMethodInvocation<?>, Callable<?>> {
    ServiceMethodInvocationExecutor executor;

    public CallableResultCase(ServiceMethodInvocationExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Callable<?> apply(ServiceMethodInvocation<?> serviceMethodInvocation) {
        Callable callableResult = () -> executor.execute(serviceMethodInvocation);
        return callableResult;
    }
}
