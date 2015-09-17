package org.restler.client;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executor;

public class RestlerConfig {

    private final URI baseUri;
    private final List<CallExecutionAdvice<?>> enhancers;
    private final Executor restlerThreadPool;

    public RestlerConfig(URI baseUri, List<CallExecutionAdvice<?>> enhancers, Executor restlerThreadPool) {
        this.baseUri = baseUri;
        this.enhancers = enhancers;
        this.restlerThreadPool = restlerThreadPool;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public List<CallExecutionAdvice<?>> getEnhancers() {
        return enhancers;
    }

    public Executor getRestlerThreadPool() {
        return restlerThreadPool;
    }
}
