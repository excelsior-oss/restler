package org.restler;

import org.restler.client.ClientFactory;
import org.restler.http.security.authorization.AuthorizationContext;

/**
 * Enriches functionality of {@link ClientFactory} with authorization management helper methods.
 */
public class Service {

    private final ClientFactory factory;
    private final AuthorizationContext serviceConfig;

    public Service(ClientFactory factory, AuthorizationContext serviceConfig) {
        this.factory = factory;
        this.serviceConfig = serviceConfig;
    }

    public <C> C produceClient(Class<C> controllerClass){
        return factory.produceClient(controllerClass);
    }

    public void authorize(){
        serviceConfig.setAuthenticationToken(serviceConfig.getAuthorizationStrategy().authorize());
    }
}
