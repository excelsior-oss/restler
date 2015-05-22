package io.github.chcat.restclientgenerator;

import io.github.chcat.restclientgenerator.factory.ClientFactory;
import io.github.chcat.restclientgenerator.http.security.authorization.AuthorizationStrategy;

import java.util.function.Function;

/**
 * Created by pasa on 22.05.2015.
 */
public class Service {

    private final ClientFactory factory;
    private final ServiceConfig config;

    public Service(ServiceConfig config, Function<ServiceConfig,ClientFactory> factoryProducer) {
        this.config = config;
        this.factory = factoryProducer.apply(config);
    }

    public void Authorize(AuthorizationStrategy authorizationStrategy){
        config.getAuthenticationStrategy().setAuthenticationToken(authorizationStrategy.authorize());
    }
}
