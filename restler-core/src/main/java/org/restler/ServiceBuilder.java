package org.restler;

import org.restler.client.CGLibClientFactory;
import org.restler.client.CachingClientFactory;
import org.restler.http.HttpServiceMethodExecutor;
import org.restler.http.HttpRequestExecutor;
import org.restler.http.RestOperationsHttpRequestExecutor;
import org.restler.http.security.authentication.AuthenticationStrategy;
import org.restler.http.security.authentication.NoAuthenticationStrategy;
import org.springframework.web.client.RestTemplate;

/**
 * Helper class for building services.
 */
public class ServiceBuilder {

    private String baseUrl;
    private HttpRequestExecutor requestExecutor = new RestOperationsHttpRequestExecutor(new RestTemplate());
    private AuthenticationStrategy authenticationStrategy = new NoAuthenticationStrategy();

    public ServiceBuilder(String baseUrl) {
            this.baseUrl = baseUrl;
    }

    public ServiceBuilder useRequestExecutor(HttpRequestExecutor requestExecutor){
        this.requestExecutor = requestExecutor;
        return this;
    }

    public ServiceBuilder useAuthenticationStrategy(AuthenticationStrategy authenticationStrategy){
        this.authenticationStrategy = authenticationStrategy;
        return this;
    }

    public Service build(){
        ServiceConfig config = new ServiceConfig(baseUrl,requestExecutor,authenticationStrategy);
        return new Service(new CachingClientFactory(new CGLibClientFactory(new HttpServiceMethodExecutor(config))));
    }

}
