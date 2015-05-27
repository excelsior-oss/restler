package org.restler.factory;

import org.restler.ServiceConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pasa on 21.05.2015.
 */
public class CachingClientFactory implements ClientFactory {

    private Map<Class<?>, Object> clients = new HashMap<>();
    private ClientFactory factory;

    public CachingClientFactory(ClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return factory.getServiceConfig();
    }

    @Override
    public <C> C produce(Class<C> type) {
        if (clients.containsKey(type)){
            return (C) clients.get(type);
        } else {
            C client = factory.produce(type);
            clients.put(type, client);
            return client;
        }
    }
}
