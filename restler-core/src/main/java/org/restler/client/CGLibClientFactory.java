package org.restler.client;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A CGLib implementation of {@link ClientFactory} that uses {@link ServiceMethodInvocationExecutor} for execution client methods.
 */
public class CGLibClientFactory implements ClientFactory {

    private final ServiceMethodInvocationExecutor executor;
    private final BiFunction<Method, Object[], ServiceMethodInvocation<?>> invocationMapper;

    private HashMap<Class<?>, Function<ServiceMethodInvocation<?>, ?>> invocationExecutors;

    public CGLibClientFactory(ServiceMethodInvocationExecutor executor, BiFunction<Method, Object[], ServiceMethodInvocation<?>> invocationMapper, Executor threadExecutor) {
        this.executor = executor;
        this.invocationMapper = invocationMapper;

        invocationExecutors = new HashMap<>();
        invocationExecutors.put(DeferredResult.class, new DeferredResultCase(threadExecutor, this.executor));
        invocationExecutors.put(Callable.class, new CallableResultCase(this.executor));
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

    private Function<ServiceMethodInvocation<?>, ?> getInvocationExecutor(Method method) {
        Function<ServiceMethodInvocation<?>, ?> invocationExecutor = invocationExecutors.get(method.getReturnType());
        if (invocationExecutor == null) {
            invocationExecutor = (ServiceMethodInvocation<?> invocation) -> executor.execute(invocation);
        }
        return invocationExecutor;
    }

    private class ControllerMethodInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
            ServiceMethodInvocation<?> invocation = invocationMapper.apply(method, args);

            return getInvocationExecutor(method).apply(invocation);
        }
    }
}
