package org.restler;

import org.restler.factory.ClientFactory;
import org.restler.http.security.authorization.AuthorizationStrategy;

/**
 * Created by pasa on 22.05.2015.
 */
public class Service {

    private final ClientFactory factory;

    public Service(ClientFactory factory) {
        this.factory = factory;
    }

    public void Authorize(AuthorizationStrategy authorizationStrategy){
        factory.getServiceConfig().getAuthenticationStrategy().setAuthenticationToken(authorizationStrategy.authorize());
    }
}
