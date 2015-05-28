package org.restler;

import org.restler.client.ClientFactory;
import org.restler.http.security.authorization.AuthorizationStrategy;

/**
 * Enriches functionality of {@link ClientFactory} with authorization management helper methods.
 */
public class Service implements ClientFactory {

    private final ClientFactory factory;

    public Service(ClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public <C> C produceClient(Class<C> controllerClass){
        return factory.produceClient(controllerClass);
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return factory.getServiceConfig();
    }

    public void authorize(AuthorizationStrategy authorizationStrategy){
        factory.getServiceConfig().getAuthenticationStrategy().setAuthenticationToken(authorizationStrategy.authorize());
    }
}
