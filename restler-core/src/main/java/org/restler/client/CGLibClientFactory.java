package org.restler.client;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * A CGLib implementation of {@link ClientFactory} that uses {@link CallExecutor} for execution client methods.
 */
@SuppressWarnings("unchecked")
public class CGLibClientFactory implements ClientFactory {

    private final CallExecutor callExecutor;
    private final BiFunction<Method, Object[], Call> mapToCall;

    public CGLibClientFactory(CallExecutor callExecutor, BiFunction<Method, Object[], Call> mapToCall) {
        this.callExecutor = callExecutor;
        this.mapToCall = mapToCall;
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

    private class ControllerMethodInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
            Call call = mapToCall.apply(method, args);
            return callExecutor.execute(call);
        }
    }

}
