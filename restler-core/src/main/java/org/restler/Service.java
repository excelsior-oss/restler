package org.restler;

import org.restler.client.CoreModule;
import org.restler.http.security.SecuritySession;

/**
 * Service it is representation of remote service, which holds session information and able
 * to produce proxies of remote services from description of that services. Supported service descriptions
 * formats are depend {@code org.restler.client.CoreModule}, that was specified for {@code org.restler.Restler}.
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
