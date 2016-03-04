package org.restler;

import org.restler.client.CoreModule;
import org.restler.http.security.SecuritySession;

/**
 * A {@code Service} is a representation of a remote service, which holds session information and is able
 * to produce proxies for remote services from descriptions of those services. Supported service description
 * formats depend on which {@code org.restler.client.CoreModule} was specified for the 
 * {@code org.restler.Restler} that produced the given {@code Service}.
 */
public class Service {

    private final CoreModule coreModule;
    private final SecuritySession session;

    Service(CoreModule module, SecuritySession session) {
        this.coreModule = module;
        this.session = session;
    }


    public <C> C produceClient(Class<C> controllerClass) {
        return coreModule.produceClient(controllerClass);
    }

    public void authorize() {
        session.authorize();
    }
}
