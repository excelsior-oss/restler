package org.restler.client;

import java.net.URI;
import java.util.List;

public class RestlerConfig {

    private final URI baseUri;
    private final List<CallExecutionAdvice<?>> enhancers;

    public RestlerConfig(URI baseUri, List<CallExecutionAdvice<?>> enhancers) {
        this.baseUri = baseUri;
        this.enhancers = enhancers;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public List<CallExecutionAdvice<?>> getEnhancers() {
        return enhancers;
    }
}
