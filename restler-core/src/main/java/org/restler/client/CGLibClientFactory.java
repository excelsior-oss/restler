package org.restler.client;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A CGLib implementation of {@link ClientFactory} that uses {@link ServiceMethodInvocationExecutor} for execution client methods.
 */
public class CGLibClientFactory implements ClientFactory {

    private final ServiceMethodInvocationExecutor executor;
    private final BiFunction<Method, Object[], ServiceMethodInvocation<?>> invocationMapper;

    private Supplier<Executor> executorSupplier;
    private Executor threadExecutor = null;

    public CGLibClientFactory(ServiceMethodInvocationExecutor executor, BiFunction<Method, Object[], ServiceMethodInvocation<?>> invocationMapper, Supplier<Executor> executorSupplier) {
        this.executor = executor;
        this.invocationMapper = invocationMapper;
        this.executorSupplier = executorSupplier;
    }

    @Override
    public <C> C produceClient(Class<C> controllerClass) {

        if (controllerClass.getDeclaredAnnotation(Controller.class) == null && controllerClass.getDeclaredAnnotation(RestController.class) == null) {
            throw new IllegalArgumentException("Not a controller");
        }

        InvocationHandler handler = new ControllerMethodInvocationHandler();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(controllerClass);
        enhancer.setCallback(handler);

        return (C) enhancer.create();
    }

    private Executor getThreadExecutor() {
        if (threadExecutor == null) {
            threadExecutor = executorSupplier.get();
        }

        return threadExecutor;
    }

    private class ControllerMethodInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {

            ServiceMethodInvocation<?> invocation = invocationMapper.apply(method, args);

            Class<?> resultType = method.getReturnType();

            if (resultType == DeferredResult.class) {
                DeferredResult deferredResult = new DeferredResult();

                getThreadExecutor().execute(() -> deferredResult.setResult(executor.execute(invocation)));

                return deferredResult;
            } else if (resultType == Callable.class) {
                DeferredResult deferredResult = new DeferredResult();

                Callable callableResult = () -> {
                    while (!deferredResult.hasResult()) ;
                    return deferredResult.getResult();
                };
                getThreadExecutor().execute(() -> deferredResult.setResult(executor.execute(invocation)));

                return callableResult;
            }

            return executor.execute(invocation);
        }
    }
}
