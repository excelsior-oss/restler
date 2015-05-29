package org.restler.client;

import org.restler.ServiceConfig;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * A CGLib implementation of {@link ClientFactory} that uses {@link ServiceMethodExecutor} for execution client methods.
 */
public class CGLibClientFactory implements ClientFactory {

    private ServiceMethodExecutor executor;
    private BiFunction<Method, Object[], ServiceMethodInvocation<?>> invocationMapper = new MethodInvocationMapper();

    public CGLibClientFactory(ServiceMethodExecutor executor) {
        this.executor = executor;
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return executor.getServiceConfig();
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
        C client = (C) enhancer.create();

        return client;
    }

    private class ControllerMethodInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {

            ServiceMethodInvocation<?> invocation = invocationMapper.apply(method, args);

            return executor.execute(invocation);
        }

    }
}
