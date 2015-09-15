package org.restler.client;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
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
 * A CGLib implementation of {@link ClientFactory} that uses {@link HttpCallExecutor} for execution client methods.
 */
@SuppressWarnings("unchecked")
public class CGLibClientFactory implements ClientFactory {

    private final HttpCallExecutor executor;
    private final BiFunction<Method, Object[], HttpCall<?>> invocationMapper;

    private final Executor threadExecutor;

    private final HashMap<Class<?>, Function<HttpCall<?>, ?>> invocationExecutors;
    private final Function<HttpCall<?>, ?> defaultInvocationExecutor;

    public CGLibClientFactory(HttpCallExecutor executor, BiFunction<Method, Object[], HttpCall<?>> invocationMapper, Executor threadExecutor) {
        this.executor = executor;
        this.invocationMapper = invocationMapper;
        this.threadExecutor = threadExecutor;

        invocationExecutors = new HashMap<>();
        invocationExecutors.put(DeferredResult.class, new DeferredResultInvocationExecutor());
        invocationExecutors.put(Callable.class, new CallableResultInvocationExecutor());

        defaultInvocationExecutor = executor::execute;
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

    private Function<HttpCall<?>, ?> getInvocationExecutor(Method method) {
        Function<HttpCall<?>, ?> invocationExecutor = invocationExecutors.get(method.getReturnType());
        if (invocationExecutor == null) {
            invocationExecutor = defaultInvocationExecutor;
        }
        return invocationExecutor;
    }

    private class ControllerMethodInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
            HttpCall<?> invocation = invocationMapper.apply(method, args);

            return getInvocationExecutor(method).apply(invocation);
        }
    }

    private class DeferredResultInvocationExecutor implements Function<HttpCall<?>, DeferredResult<?>> {

        @Override
        public DeferredResult apply(HttpCall<?> httpCall) {
            DeferredResult deferredResult = new DeferredResult();
            threadExecutor.execute(() -> deferredResult.setResult(executor.execute(httpCall)));
            return deferredResult;
        }
    }

    private class CallableResultInvocationExecutor implements Function<HttpCall<?>, Callable<?>> {

        @Override
        public Callable<?> apply(HttpCall<?> httpCall) {
            return () -> executor.execute(httpCall);
        }
    }
}
