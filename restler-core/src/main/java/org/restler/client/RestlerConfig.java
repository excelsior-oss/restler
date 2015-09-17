package org.restler.client;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executor;

public class RestlerConfig {

    private final URI baseUri;
    private final List<CallEnhancer> enhancers;
    private final Executor restlerThreadPool;

    public RestlerConfig(URI baseUri, List<CallEnhancer> enhancers, Executor restlerThreadPool) {
        this.baseUri = baseUri;
        this.enhancers = enhancers;
        this.restlerThreadPool = restlerThreadPool;
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
}
