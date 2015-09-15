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
 * A CGLib implementation of {@link ClientFactory} that uses {@link CallExecutor} for execution client methods.
 */
@SuppressWarnings("unchecked")
public class CGLibClientFactory implements ClientFactory {

    private final CallExecutor callExecutor;
    private final BiFunction<Method, Object[], Call> mapToCall;

    private final Executor threadExecutor;

    private final HashMap<Class<?>, Function<Call, ?>> invocationExecutors;
    private final Function<Call, ?> defaultInvocationExecutor;

    public CGLibClientFactory(CallExecutor callExecutor, BiFunction<Method, Object[], Call> mapToCall, Executor threadExecutor) {
        this.callExecutor = callExecutor;
        this.mapToCall = mapToCall;
        this.threadExecutor = threadExecutor;

        invocationExecutors = new HashMap<>();
        invocationExecutors.put(DeferredResult.class, new DeferredResultInvocationExecutor());
        invocationExecutors.put(Callable.class, new CallableResultInvocationExecutor());

        defaultInvocationExecutor = callExecutor::execute;
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

    private Function<Call, ?> getInvocationExecutor(Method method) {
        Function<Call, ?> invocationExecutor = invocationExecutors.get(method.getReturnType());
        if (invocationExecutor == null) {
            invocationExecutor = defaultInvocationExecutor;
        }
        return invocationExecutor;
    }

    private class ControllerMethodInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
            Call invocation = mapToCall.apply(method, args);

            return getInvocationExecutor(method).apply(invocation);
        }
    }

    private class DeferredResultInvocationExecutor implements Function<Call, DeferredResult<?>> {

        @Override
        public DeferredResult<?> apply(Call httpCall) {
            DeferredResult deferredResult = new DeferredResult();
            threadExecutor.execute(() -> deferredResult.setResult(callExecutor.execute(httpCall)));
            return deferredResult;
        }
    }

    private class CallableResultInvocationExecutor implements Function<Call, Callable<?>> {

        @Override
        public Callable<?> apply(Call httpCall) {
            return () -> callExecutor.execute(httpCall);
        }
    }
}
