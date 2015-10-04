package org.restler.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link ClientFactory} that delegates production to an inner factory and caches its results.
 */
public class CachingClientFactory implements ClientFactory {

    private final ConcurrentHashMap<Class<?>, Object> clients = new ConcurrentHashMap<>();
    private final ClientFactory factory;

    /**
     * Creates a caching wrapper of a {@link ClientFactory} instance.
     *
     * @param factory an instance used for production if there is no cached result for a given input.
     */
    public CachingClientFactory(ClientFactory factory) {
        this.factory = factory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> C produceClient(Class<C> controllerClass) {
        return (C) clients.computeIfAbsent(controllerClass, factory::produceClient);
    }

}
