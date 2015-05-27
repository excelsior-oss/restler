package org.restler.factory;

import org.restler.ServiceConfig;

public interface ClientFactory {

    ServiceConfig getServiceConfig();

    <C> C produce(Class<C> type);
}
