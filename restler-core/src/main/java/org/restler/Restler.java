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
import java.util.function.Function;

/**
 * The entry point into library
 */
public class Restler {

    public static Executor restlerExecutor = Executors.newCachedThreadPool();

    private final List<CallExecutionAdvice<?>> enhancers = new ArrayList<>();
    private final UriBuilder uriBuilder;

    private AuthenticationStrategy authenticationStrategy;
    private AuthorizationStrategy authorizationStrategy;

    private boolean autoAuthorize = true;
    private Function<RestlerConfig, CoreModule> createCoreModule;

    public Restler(String baseUrl, Function<RestlerConfig, CoreModule> coreModule) {
        uriBuilder = new UriBuilder(baseUrl);
        this.createCoreModule = coreModule;
    }

    public Restler(URI baseUrl, Function<RestlerConfig, CoreModule> coreModule) {
        uriBuilder = new UriBuilder(baseUrl);
        this.createCoreModule = coreModule;
    }

    public Restler authenticationStrategy(AuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
        return this;
    }

    public Restler authorizationStrategy(AuthorizationStrategy authorizationStrategy) {
        this.authorizationStrategy = authorizationStrategy;
        return this;
    }

    public Restler cookieBasedAuthentication() {
        return authenticationStrategy(new CookieAuthenticationStrategy());
    }

    public Restler httpBasicAuthentication(String login, String password) {
        authorizationStrategy(new BasicAuthorizationStrategy(login, password));
        return authenticationStrategy(new HttpBasicAuthenticationStrategy());
    }

    public Restler autoAuthorize(boolean autoAuthorize) {
        this.autoAuthorize = autoAuthorize;
        return this;
    }

    public Restler addEnhancer(CallExecutionAdvice<?> enhancer) {
        enhancers.add(enhancer);
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

        List<CallExecutionAdvice<?>> advices = new ArrayList<>(enhancers);
        advices.add(new CallableHandler());
        if (authenticationStrategy != null) {
            advices.add(new AuthenticatingExecutionAdvice(session));
        }

        CachingClientFactory factory = new CachingClientFactory(new CGLibClientFactory(createCoreModule.apply(new RestlerConfig(uriBuilder.build(), advices))));

        return new Service(factory, session);
    }

}

