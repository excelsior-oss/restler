package org.restler;

import com.fasterxml.jackson.databind.Module;
import org.restler.client.*;
import org.restler.http.HttpCallExecutor;
import org.restler.http.RequestExecutor;
import org.restler.http.security.AuthenticatingExecutionAdvice;
import org.restler.http.security.SecuritySession;
import org.restler.http.security.authentication.AuthenticationStrategy;
import org.restler.http.security.authentication.CookieAuthenticationStrategy;
import org.restler.http.security.authentication.HttpBasicAuthenticationStrategy;
import org.restler.http.security.authorization.AuthorizationStrategy;
import org.restler.http.security.authorization.BasicAuthorizationStrategy;
import org.restler.spring.ControllerMethodInvocationMapper;
import org.restler.spring.RestOperationsRequestExecutor;
import org.restler.util.UriBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Helper class for building services.
 */
public class ServiceBuilder {

    private final List<Module> jacksonModules = new ArrayList<>();
    private final UriBuilder uriBuilder;
    private ParameterResolver paramResolver = ParameterResolver.valueOfParamResolver();
    private Optional<Executor> threadExecutor = Optional.empty();
    private Optional<RequestExecutor> requestExecutor = Optional.empty();
    private CallExecutionAdvice errorMapper = null;

    private AuthenticationStrategy authenticationStrategy;
    private AuthorizationStrategy authorizationStrategy;

    private boolean autoAuthorize = true;

    public ServiceBuilder(String baseUrl) {
        uriBuilder = new UriBuilder(baseUrl);
    }

    public ServiceBuilder(URI baseUrl) {
        uriBuilder = new UriBuilder(baseUrl);
    }

    public ServiceBuilder requestExecutor(RequestExecutor requestExecutor) {
        this.requestExecutor = Optional.ofNullable(requestExecutor);
        return this;
    }

    public ServiceBuilder threadExecutor(Executor threadExecutor) {
        this.threadExecutor = Optional.of(threadExecutor);
        return this;
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

    public ServiceBuilder parametersResolver(ParameterResolver parametersResolver) {
        this.paramResolver = parametersResolver;
        return this;
    }

    public ServiceBuilder addJacksonModule(Module module) {
        jacksonModules.add(module);
        return this;
    }

    public Service build() throws RestlerException {

        validate();

        SecuritySession session = new SecuritySession(authorizationStrategy, authenticationStrategy, autoAuthorize);
        ControllerMethodInvocationMapper invocationMapper = new ControllerMethodInvocationMapper(uriBuilder.build(), paramResolver);

        List<CallExecutionAdvice> advices = new ArrayList<>();
        if (authenticationStrategy != null) {
            advices.add(new AuthenticatingExecutionAdvice(session));
        }
        if (errorMapper != null) {
            advices.add(errorMapper);
        }
        CallExecutor executor = new HttpCallExecutor(requestExecutor.orElseGet(this::defaultRequestExecutor));
        CallExecutionChain chain = new CallExecutionChain(executor, advices);

        CachingClientFactory factory = new CachingClientFactory(new CGLibClientFactory(chain, invocationMapper, threadExecutor.orElseGet(Executors::newCachedThreadPool)));

        return new Service(factory, session);
    }

    private void validate() throws RestlerException {
        if (requestExecutor.isPresent() && jacksonModules.size() > 0) {
            throw new RestlerException("Jackson modules are not used with custom request executor. Please specify request executor either jackson modules");
        }
    }

    private RestOperationsRequestExecutor defaultRequestExecutor() {
        RestTemplate restTemplate = new RestTemplate();

        List<MappingJackson2HttpMessageConverter> jacksonConverters = restTemplate.getMessageConverters().stream().
                filter(converter -> converter instanceof MappingJackson2HttpMessageConverter).
                map(converter -> (MappingJackson2HttpMessageConverter) converter).
                collect(Collectors.toList());

        jacksonModules.stream().forEach(module ->
                jacksonConverters.forEach(converter ->
                        converter.getObjectMapper().registerModule(module)));
        return new RestOperationsRequestExecutor(restTemplate);
    }

}

