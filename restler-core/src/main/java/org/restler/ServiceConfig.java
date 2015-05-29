package org.restler;

import org.restler.http.RequestExecutor;
import org.restler.http.security.authentication.AuthenticationContext;
import org.restler.http.security.authentication.AuthenticationStrategy;

/**
 * Description of a service.
 */
public class ServiceConfig implements AuthenticationContext {

    private final String baseUrl;
    private final RequestExecutor requestExecutor;
    private final AuthenticationStrategy authenticationStrategy;
    private Object authenticationToken;

    public ServiceConfig(String baseUrl, RequestExecutor requestExecutor, AuthenticationStrategy authenticationStrategy) {
        this.baseUrl = baseUrl;
        this.requestExecutor = requestExecutor;
        this.authenticationStrategy = authenticationStrategy;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public RequestExecutor getRequestExecutor() {
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
