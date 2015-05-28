package org.restler;

import org.restler.http.RequestExecutor;
import org.restler.http.security.authentication.AuthenticationStrategy;

/**
 * Description of a service.
 */
public class ServiceConfig {

    private final String baseUrl;
    private final RequestExecutor requestExecutor;
    private final AuthenticationStrategy authenticationStrategy;

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

    public AuthenticationStrategy getAuthenticationStrategy() {
        return authenticationStrategy;
    }
}
