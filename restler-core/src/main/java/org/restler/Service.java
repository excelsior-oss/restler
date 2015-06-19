package org.restler;

import org.restler.client.ClientFactory;
import org.restler.http.security.SecuritySession;

/**
 * Enriches functionality of {@link ClientFactory} with authorization management helper methods.
 */
public class Service {

    private final ClientFactory factory;
    private final SecuritySession session;

    Service(ClientFactory factory, SecuritySession session) {
        this.factory = factory;
        this.session = session;
    }

    public <C> C produceClient(Class<C> controllerClass) {
        return factory.produceClient(controllerClass);
    }

    public void authorize() {
        session.authorize();
    }
}
