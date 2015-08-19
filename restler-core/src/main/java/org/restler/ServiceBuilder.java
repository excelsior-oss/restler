package org.restler;

import com.fasterxml.jackson.databind.Module;
import org.restler.client.CGLibClientFactory;
import org.restler.client.CachingClientFactory;
import org.restler.client.ControllerMethodInvocationMapper;
import org.restler.client.ParameterResolver;
import org.restler.http.*;
import org.restler.http.error.ClassNameErrorMappingRequestExecutionAdvice;
import org.restler.http.security.AuthenticatingExecutionAdvice;
import org.restler.http.security.ReauthorizingExecutionAdvice;
import org.restler.http.security.SecuritySession;
import org.restler.http.security.authentication.AuthenticationStrategy;
import org.restler.http.security.authentication.CookieAuthenticationStrategy;
import org.restler.http.security.authentication.HttpBasicAuthenticationStrategy;
import org.restler.http.security.authorization.AuthorizationStrategy;
import org.restler.http.security.authorization.BasicAuthorizationStrategy;
import org.restler.util.UriBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Helper class for building services.
 */
public class ServiceBuilder {

    private final UriBuilder uriBuilder;
    private final RestTemplate restTemplate = new RestTemplate();
    private ParameterResolver paramResolver = ParameterResolver.valueOfParamResolver();
    private java.util.concurrent.Executor threadExecutor = Executors.newCachedThreadPool();
    private Executor executor = new RestOperationsExecutor(restTemplate);
    private ExecutionAdvice errorMapper = null;

    private AuthenticationStrategy authenticationStrategy;
    private AuthorizationStrategy authorizationStrategy;

    private boolean reauthorize = false;
    private boolean autoAuthorize = true;

    public ServiceBuilder(String baseUrl) {
        uriBuilder = new UriBuilder(baseUrl);
    }

    public ServiceBuilder(URI baseUrl) {
        uriBuilder = new UriBuilder(baseUrl);
    }

    public ServiceBuilder useExecutor(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "Provide an executor");
        return this;
    }

    public ServiceBuilder useAuthenticationStrategy(AuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
        return this;
    }

    public ServiceBuilder useAuthorizationStrategy(AuthorizationStrategy authorizationStrategy) {
        this.authorizationStrategy = authorizationStrategy;
        return this;
    }

    public ServiceBuilder useCookieBasedAuthentication() {
        return useAuthenticationStrategy(new CookieAuthenticationStrategy());
    }

    public ServiceBuilder useHttpBasicAuthentication(String login, String password) {
        useAuthorizationStrategy(new BasicAuthorizationStrategy(login, password));
        return useAuthenticationStrategy(new HttpBasicAuthenticationStrategy());
    }

    public ServiceBuilder reauthorizeRequestsOnForbidden(boolean reauthorize) {
        this.reauthorize = reauthorize;
        return this;
    }

    public ServiceBuilder autoAuthorize(boolean autoAuthorize) {
        this.autoAuthorize = autoAuthorize;
        return this;
    }

    public ServiceBuilder useErrorMapper(ExecutionAdvice errorMapper) {
        this.errorMapper = errorMapper;
        return this;
    }

    public ServiceBuilder useClassNameExceptionMapper() {
        return useErrorMapper(new ClassNameErrorMappingRequestExecutionAdvice());
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

    public ServiceBuilder withParametersResolver(ParameterResolver parametersResolver) {
        this.paramResolver = parametersResolver;
        return this;
    }

    public ServiceBuilder registerJacksonModule(Module module) {
        restTemplate.getMessageConverters().stream().
                filter(converter -> converter instanceof MappingJackson2HttpMessageConverter).
                forEach((converter) -> ((MappingJackson2HttpMessageConverter) converter).getObjectMapper().registerModule(module));
        return this;
    }

    public Service build() {

        SecuritySession session = new SecuritySession(authorizationStrategy, authenticationStrategy, autoAuthorize);
        List<ExecutionAdvice> advices = new ArrayList<>();

        if (reauthorize) {
            Objects.requireNonNull(authorizationStrategy, "Specify authorization strategy with useAuthorizationStrategy() method");
            advices.add(new ReauthorizingExecutionAdvice(session));
        }

        if (authenticationStrategy != null) {
            advices.add(new AuthenticatingExecutionAdvice(session));
        }

        if (errorMapper != null) {
            advices.add(errorMapper);
        }

        ExecutionChain chain = new ExecutionChain(executor, advices);

        ControllerMethodInvocationMapper invocationMapper = new ControllerMethodInvocationMapper(uriBuilder.build(), paramResolver);
        HttpServiceMethodInvocationExecutor executor = new HttpServiceMethodInvocationExecutor(chain);
        CachingClientFactory factory = new CachingClientFactory(new CGLibClientFactory(executor, invocationMapper, threadExecutor));

        return new Service(factory, session);
    }

}

