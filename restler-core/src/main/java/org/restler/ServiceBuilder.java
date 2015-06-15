package org.restler;

import org.restler.client.CGLibClientFactory;
import org.restler.client.CachingClientFactory;
import org.restler.client.MethodInvocationMapper;
import org.restler.http.HttpRequestExecutor;
import org.restler.http.HttpServiceMethodExecutor;
import org.restler.http.SimpleHttpRequestExecutor;
import org.restler.http.error.ClassNameErrorMappingRequestExecutor;
import org.restler.http.security.authentication.CookieAuthenticatingRequestExecutor;
import org.restler.http.security.authentication.HTTPBasicAuthenticationRequestExecutor;
import org.restler.http.security.authorization.AuthorizationStrategy;
import org.restler.http.security.authorization.BasicAuthorizationStrategy;
import org.restler.http.security.authorization.ReauthorizingRequestExecutor;
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
    private BiFunction<HttpRequestExecutor, Session, HttpRequestExecutor> exceptionMappingExecutor;
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
        Objects.requireNonNull(authorizationStrategy, "Specify authorization strategy with useAuthorizationStrategy() method");
        authenticationExecutor = CookieAuthenticatingRequestExecutor::new;
        return this;
    }

    public ServiceBuilder useHTTPBasicAuthentication(String login, String password) {
        AuthorizationStrategy basicAuth = new BasicAuthorizationStrategy(login, password);
        useAuthorizationStrategy(basicAuth);

        authenticationExecutor = HTTPBasicAuthenticationRequestExecutor::new;
        return this;
    }

    public ServiceBuilder reauthorizeRequestsOnForbidden() {
        Objects.requireNonNull(authorizationStrategy, "Specify authorization strategy with useAuthorizationStrategy() method");
        reauthorizingExecutor = ReauthorizingRequestExecutor::new;
        return this;
    }

    public ServiceBuilder useClassNameExceptionMapper() {
        exceptionMappingExecutor = ((delegate, config) -> new ClassNameErrorMappingRequestExecutor(delegate));
        return this;
    }

    public Service build() {
        HttpRequestExecutor executor = requestExecutor;
        Session session = new Session(authorizationStrategy);
        if (exceptionMappingExecutor != null) {
            executor = exceptionMappingExecutor.apply(executor, session);
        }
        if (authenticationExecutor != null) {
            executor = authenticationExecutor.apply(executor, session);
        }
        if (reauthorizingExecutor != null) {
            executor = reauthorizingExecutor.apply(executor, session);
        }
        return new Service(new CachingClientFactory(new CGLibClientFactory(new HttpServiceMethodExecutor(executor), new MethodInvocationMapper(baseUrl))), session);
    }

}
