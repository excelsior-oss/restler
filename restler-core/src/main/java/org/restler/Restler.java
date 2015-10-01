package org.restler;

import org.restler.client.*;
import org.restler.http.security.AuthenticatingEnhancer;
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
import java.util.stream.Collectors;

/**
 * The entry point into library
 */
public class Restler {

    public static final Executor defaultThreadPool = Executors.newCachedThreadPool();

    private final List<CallEnhancer> enhancers = new ArrayList<>();
    private final List<Function<RestlerConfig, List<CallEnhancer>>> enhancerFactories = new ArrayList<>();
    private final UriBuilder uriBuilder;

    private AuthenticationStrategy authenticationStrategy;
    private AuthorizationStrategy authorizationStrategy;
    private Executor threadPool = Restler.defaultThreadPool;

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

    public Restler threadPool(Executor executor) {
        this.threadPool = executor;
        return this;
    }

    public Restler addEnhancer(CallEnhancer enhancer) {
        enhancers.add(enhancer);
        return this;
    }

    public Restler add(Function<RestlerConfig, List<CallEnhancer>> enhancerFactory) {
        enhancerFactories.add(enhancerFactory);
        return this;
    }

    public Restler scheme(String scheme) {
        uriBuilder.scheme(scheme);
        return this;
    }

    public Restler host(String host) {
        uriBuilder.host(host);
        return this;
    }

    public Restler port(int port) {
        uriBuilder.port(port);
        return this;
    }

    public Restler path(String path) {
        uriBuilder.path(path);
        return this;
    }

    public Restler replacePath(String path) {
        uriBuilder.replacePath(path);
        return this;
    }

    public Service build() throws RestlerException {

        SecuritySession session = new SecuritySession(authorizationStrategy, authenticationStrategy, autoAuthorize);

        List<CallEnhancer> enhancers = new ArrayList<>(this.enhancers);
        RestlerConfig config = new RestlerConfig(uriBuilder.build(), enhancers, threadPool, session);
        List<CallEnhancer> additionalEnhancers = enhancerFactories.stream().flatMap((enhancerFactory) -> enhancerFactory.apply(config).stream()).collect(Collectors.toList());
        enhancers.addAll(additionalEnhancers);
        if (authenticationStrategy != null) {
            enhancers.add(new AuthenticatingEnhancer(session));
        }

        CachingClientFactory factory = new CachingClientFactory(new CGLibClientFactory(createCoreModule.apply(new RestlerConfig(uriBuilder.build(), enhancers, threadPool, session))));

        return new Service(factory, session);
    }

}

