package org.restler;

import org.restler.client.*;
import org.restler.http.security.AuthenticatingExecutionAdvice;
import org.restler.http.security.SecuritySession;
import org.restler.http.security.authentication.AuthenticationStrategy;
import org.restler.http.security.authentication.CookieAuthenticationStrategy;
import org.restler.http.security.authentication.HttpBasicAuthenticationStrategy;
import org.restler.http.security.authorization.AuthorizationStrategy;
import org.restler.http.security.authorization.BasicAuthorizationStrategy;
import org.restler.util.UriBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Helper class for building services.
 */
public class ServiceBuilder {

    public static Executor restlerExecutor = Executors.newCachedThreadPool();

    private final UriBuilder uriBuilder;
    private CallExecutionAdvice errorMapper = null;

    private AuthenticationStrategy authenticationStrategy;
    private AuthorizationStrategy authorizationStrategy;

    private boolean autoAuthorize = true;
    private CoreModuleFactory coreModuleFactory;

    public ServiceBuilder(String baseUrl, CoreModuleFactory coreModule) {
        uriBuilder = new UriBuilder(baseUrl);
        this.coreModuleFactory = coreModule;
    }

    public ServiceBuilder(URI baseUrl, CoreModuleFactory coreModule) {
        uriBuilder = new UriBuilder(baseUrl);
        this.coreModuleFactory = coreModule;
    }

    public ServiceBuilder authenticationStrategy(AuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
        return this;
    }

    public ServiceBuilder authorizationStrategy(AuthorizationStrategy authorizationStrategy) {
        this.authorizationStrategy = authorizationStrategy;
        return this;
    }

    public ServiceBuilder cookieBasedAuthentication() {
        return authenticationStrategy(new CookieAuthenticationStrategy());
    }

    public ServiceBuilder httpBasicAuthentication(String login, String password) {
        authorizationStrategy(new BasicAuthorizationStrategy(login, password));
        return authenticationStrategy(new HttpBasicAuthenticationStrategy());
    }

    public ServiceBuilder autoAuthorize(boolean autoAuthorize) {
        this.autoAuthorize = autoAuthorize;
        return this;
    }

    public ServiceBuilder errorMapper(CallExecutionAdvice errorMapper) {
        this.errorMapper = errorMapper;
        return this;
    }

    public void scheme(String scheme) {
        uriBuilder.scheme(scheme);
    }

    public void host(String host) {
        uriBuilder.host(host);
    }

    public void port(int port) {
        uriBuilder.port(port);
    }

    public void path(String path) {
        uriBuilder.path(path);
    }

    public Service build() throws RestlerException {

        SecuritySession session = new SecuritySession(authorizationStrategy, authenticationStrategy, autoAuthorize);

        List<CallExecutionAdvice<?>> advices = new ArrayList<>();
        advices.add(new CallableHandler());
        if (authenticationStrategy != null) {
            advices.add(new AuthenticatingExecutionAdvice(session));
        }
        if (errorMapper != null) {
            advices.add(errorMapper);
        }

        CachingClientFactory factory = new CachingClientFactory(new CGLibClientFactory(coreModuleFactory.createModule(uriBuilder.build(), advices)));

        return new Service(factory, session);
    }

}

