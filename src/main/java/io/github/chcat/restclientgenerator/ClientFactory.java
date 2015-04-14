package io.github.chcat.restclientgenerator;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pasa on 14.04.2015.
 */
public abstract class ClientFactory {

    protected Map<Class<?>, Object> clients = new HashMap<>();

    public <C> C client(Class<C> type) {

        if (clients.containsKey(type)){
            return (C) clients.get(type);
        }

        if (type.getDeclaredAnnotation(Controller.class) == null) {
            throw new IllegalArgumentException("Not a controller");
        }

        MethodInterceptor interceptor = new ControllerMethodInterceptor(provideControllerMethodExecutor());

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setCallback(interceptor);
        C client = (C) enhancer.create();

        clients.put(type, client);

        return client;
    }

    protected abstract ControllerMethodExecutor provideControllerMethodExecutor();

}
