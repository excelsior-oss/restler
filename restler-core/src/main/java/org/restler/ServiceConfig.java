package org.restler;

import org.restler.http.HttpRequestExecutor;
import org.restler.http.security.authentication.AuthenticationContext;
import org.restler.http.security.authentication.AuthenticationStrategy;

/**
 * Description of a service.
 */
public class ServiceConfig implements AuthenticationContext {

    private final String baseUrl;
    private final HttpRequestExecutor requestExecutor;
    private final AuthenticationStrategy authenticationStrategy;
    private Object authenticationToken;

    public ServiceConfig(String baseUrl, HttpRequestExecutor requestExecutor, AuthenticationStrategy authenticationStrategy) {
        this.baseUrl = baseUrl;
        this.requestExecutor = requestExecutor;
        this.authenticationStrategy = authenticationStrategy;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public HttpRequestExecutor getRequestExecutor() {
        return requestExecutor;
    }

    @Override
    public AuthenticationStrategy getAuthenticationStrategy() {
        return authenticationStrategy;
    }

    @Override
    public Object getAuthenticationToken() {
        return authenticationToken;
    }

    @Override
    public void setAuthenticationToken(Object authenticationToken) {
        this.authenticationToken = authenticationToken;
    }
}
