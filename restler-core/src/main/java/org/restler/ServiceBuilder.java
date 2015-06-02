package org.restler;

import org.restler.client.CGLibClientFactory;
import org.restler.client.CachingClientFactory;
import org.restler.client.MethodInvocationMapper;
import org.restler.http.*;
import org.restler.http.security.authentication.CookieAuthenticatingRequestExecutor;
import org.restler.http.security.authorization.AuthorizationStrategy;
import org.restler.http.security.authorization.ReauthorizingReqExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Helper class for building services.
 */
public class ServiceBuilder {

    private String baseUrl;
    private HttpRequestExecutor requestExecutor = new SimpleHttpRequestExecutor(new RestTemplate());
    private BiFunction<HttpRequestExecutor, Session, HttpRequestExecutor> authenticationExecutor;
    private BiFunction<HttpRequestExecutor, Session, HttpRequestExecutor> reauthorizingExecutor;
    private AuthorizationStrategy authorizationStrategy;

    public ServiceBuilder(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ServiceBuilder useRequestExecutor(HttpRequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
        return this;
    }

    public ServiceBuilder useAuthorizationStrategy(AuthorizationStrategy authorizationStrategy) {
        this.authorizationStrategy = authorizationStrategy;
        return this;
    }
    public ServiceBuilder useCookieBasedAuthentication() {
        authenticationExecutor = ((delegate, config) -> new CookieAuthenticatingRequestExecutor(config, delegate));
        return this;
    }

    public ServiceBuilder reauthorizeRequestsOnForbidden() {
        Objects.requireNonNull(authorizationStrategy, "Specify authorization strategy with useAuthorizationStrategy() method");
        reauthorizingExecutor = ((delegate, config) -> new ReauthorizingReqExecutor(delegate, config, authorizationStrategy));
        return this;
    }

    public Service build() {
        HttpRequestExecutor executor = requestExecutor;
        Session config = new Session(authorizationStrategy);
        if (authenticationExecutor != null) {
            executor = authenticationExecutor.apply(executor, config);
        }
        if (reauthorizingExecutor != null) {
            executor = reauthorizingExecutor.apply(executor, config);
        }
        return new Service(new CachingClientFactory(new CGLibClientFactory(new HttpServiceMethodExecutor(executor), new MethodInvocationMapper(baseUrl))), config);
    }

}
