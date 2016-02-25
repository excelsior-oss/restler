package org.restler;

import org.restler.client.CallEnhancer;
import org.restler.client.ClientFactory;
import org.restler.http.security.SecuritySession;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * RestlerConfig is used to pass configuration to {@code org.restler.client.CoreModule} implementations
 */
public class RestlerConfig {

    private final URI baseUri;
    private final List<CallEnhancer> enhancers;
    private final Executor restlerThreadPool;
    private final SecuritySession securitySession;
    private final ClientFactory clientFactory;

    public RestlerConfig(URI baseUri, List<CallEnhancer> enhancers, Executor restlerThreadPool, SecuritySession securitySession, ClientFactory clientFactory) {
        this.baseUri = baseUri;
        this.enhancers = enhancers;
        this.restlerThreadPool = restlerThreadPool;
        this.securitySession = securitySession;
        this.clientFactory = clientFactory;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public List<CallEnhancer> getEnhancers() {
        return enhancers;
    }

    public Executor getRestlerThreadPool() {
        return restlerThreadPool;
    }

    public SecuritySession getSecuritySession() {
        return securitySession;
    }

    public ClientFactory getClientFactory() {
        return clientFactory;
    }
}
