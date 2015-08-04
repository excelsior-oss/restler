package org.restler.client;

import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Map;

public class ServiceMethodInvocation<T> {

    private final URI baseUrl;
    private final ServiceMethod<T> method;
    private final Object requestBody;
    private final Map<String, ?> pathVariables;
    private final MultiValueMap<String, String> requestParams;

    public ServiceMethodInvocation(URI baseUrl, ServiceMethod<T> method, Object requestBody, Map<String, ?> pathVariables, MultiValueMap<String, String> requestParams) {
        this.baseUrl = baseUrl;
        this.method = method;
        this.requestBody = requestBody;
        this.pathVariables = pathVariables;
        this.requestParams = requestParams;
    }

    public URI getBaseUrl() {
        return baseUrl;
    }

    public ServiceMethod<T> getMethod() {
        return method;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public Map<String, ?> getPathVariables() {
        return pathVariables;
    }

    public MultiValueMap<String, String> getRequestParams() {
        return requestParams;
    }

}
