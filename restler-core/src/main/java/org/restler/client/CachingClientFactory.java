package org.restler.client;

import org.restler.ServiceConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link ClientFactory} that delegates production to an inner factory and caches its results.
 */
public class CachingClientFactory implements ClientFactory {

    private Map<Class<?>, Object> clients = new HashMap<>();
    private ClientFactory factory;

    /**
     * Creates a caching wrapper of a {@link ClientFactory} instance.
     * @param factory an instance used for production if there is no cached result for a given input.
     */
    public CachingClientFactory(ClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return factory.getServiceConfig();
    }

    @Override
    public <C> C produceClient(Class<C> controllerClass) {
        if (clients.containsKey(controllerClass)){
            return (C) clients.get(controllerClass);
        } else {
            C client = factory.produceClient(controllerClass);
            clients.put(controllerClass, client);
            return client;
        }
    }
}
