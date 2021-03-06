package org.restler;

import org.restler.client.*;
import org.restler.http.security.AuthenticatingEnhancer;
import org.restler.http.security.SecuritySession;
import org.restler.http.security.authentication.AuthenticationStrategy;
import org.restler.http.security.authentication.CookieAuthenticationStrategy;
import org.restler.http.security.authentication.HttpBasicAuthenticationStrategy;
import org.restler.http.security.authorization.AuthorizationStrategy;
import org.restler.http.security.authorization.FormAuthorizationStrategy;
import org.restler.util.UriBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.restler.http.security.authentication.CookieAuthenticationStrategy.JSESSIONID;

/**
 * The main entry point into the library. A {@code Restler} is actually a builder for {@code Service}s. Instances of
 * {@code Restler} can be reused to produce slightly different services - for example, two services that differ only
 * by their base URLs, or two services that differ only in authorization settings.
 * <p>
 * {@code Restler} could be extended in three main ways:
 * </p>
 * <ul>
 *     <li>Each {@code Restler} is configured with {@code CoreModule} that defines supported service description formats
 *     and provides CgLib {@code net.sf.cglib.proxy.InvocationHandler} that will be used to proxy remote services</li>
 *     <li>A list of {@code CallEnhancer}s instances and/or suppliers may be provided to enable additional capabilities</li>
 *     <li>Custom {@code AuthenticationStrategy} and {@code AuthorizationStrategy} may be provided to support custom security mechanisms</li>
 * </ul>
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

    /**
     * Instantiates a new {@code Restler} with a given base URL and a function that is able to produce a {@code CoreModule} from a {@code RestlerConfig}. The core module
     * producing function should take into account all data provided by {@code RestlerConfig}, especially the list of provided {@code CallEnhancer} instances -
     * all of them should be able to take part in request processing.
     */
    public Restler(String baseUrl, Function<RestlerConfig, CoreModule> coreModule) {
        uriBuilder = new UriBuilder(baseUrl);
        this.createCoreModule = coreModule;
    }

    /**
     * Instantiates a new {@code Restler} with a given base URL and a function that is able to produce a {@code CoreModule} from a {@code RestlerConfig}. The core module
     * producing function should take into account all data provided by {@code RestlerConfig}, especially the list of provided {@code CallEnhancer} instances -
     * all of them should be able to take part in request processing.
     */
    public Restler(URI baseUrl, Function<RestlerConfig, CoreModule> coreModule) {
        uriBuilder = new UriBuilder(baseUrl);
        this.createCoreModule = coreModule;
    }

    public Restler authenticationStrategy(AuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
        return this;
    }

    public Restler authorizationStrategy(AuthorizationStrategy authorizationStrategy, AuthenticationStrategy authenticationStrategy) {
        this.authorizationStrategy = authorizationStrategy;
        return authenticationStrategy(authenticationStrategy);
    }

    public Restler formAuthentication(URI loginUrl, String userName, String password) {
        return formAuthentication(loginUrl, "username", userName, "password", password, JSESSIONID);
    }
    public Restler formAuthentication(URI loginUrl, String userNameParam, String userName, String passwordParam, String password, String sessionCookieName) {
        return authorizationStrategy(new FormAuthorizationStrategy(loginUrl, userNameParam, userName, passwordParam, password, sessionCookieName), new CookieAuthenticationStrategy());
    }

    public Restler httpBasicAuthentication(String login, String password) {
        return authenticationStrategy(new HttpBasicAuthenticationStrategy(login, password));
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

        return new Service(createCoreModule.apply(config), session);
    }

}

