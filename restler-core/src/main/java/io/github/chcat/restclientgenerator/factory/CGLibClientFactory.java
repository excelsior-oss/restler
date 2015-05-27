package io.github.chcat.restclientgenerator.factory;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.stereotype.Controller;

/**
 * Created by pasa on 14.04.2015.
 */
public class CGLibClientFactory implements ClientFactory {

    private ControllerMethodExecutor executor;

    public CGLibClientFactory(ControllerMethodExecutor executor) {
        this.executor = executor;
    }

    @Override
    public <C> C produce(Class<C> type) {

        if (type.getDeclaredAnnotation(Controller.class) == null) {
            throw new IllegalArgumentException("Not a controller");
        }

        MethodInterceptor interceptor = new ControllerMethodInterceptor(executor);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setCallback(interceptor);
        C client = (C) enhancer.create();

        return client;
    }

}
