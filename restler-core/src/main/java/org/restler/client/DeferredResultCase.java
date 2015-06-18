package org.restler.client;

import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.Executor;
import java.util.function.Function;

public class DeferredResultCase implements Function<ServiceMethodInvocation<?>, DeferredResult<?>> {
    private Executor threadExecutor;
    private ServiceMethodInvocationExecutor executor;

    public DeferredResultCase(Executor threadExecutor, ServiceMethodInvocationExecutor executor) {
        this.threadExecutor = threadExecutor;
        this.executor = executor;
    }

    @Override
    public DeferredResult apply(ServiceMethodInvocation<?> serviceMethodInvocation) {
        DeferredResult deferredResult = new DeferredResult();
        threadExecutor.execute(() -> deferredResult.setResult(executor.execute(serviceMethodInvocation)));
        return deferredResult;
    }
}
