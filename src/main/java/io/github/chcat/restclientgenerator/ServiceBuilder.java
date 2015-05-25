package io.github.chcat.restclientgenerator;

import io.github.chcat.restclientgenerator.factory.CGLibClientFactory;
import io.github.chcat.restclientgenerator.factory.CachingClientFactory;
import io.github.chcat.restclientgenerator.factory.ClientFactory;
import io.github.chcat.restclientgenerator.http.HttpControllerMethodExecutor;
import io.github.chcat.restclientgenerator.http.RequestExecutor;
import io.github.chcat.restclientgenerator.http.RestOperationsRequestExecutor;
import io.github.chcat.restclientgenerator.http.security.authentication.AuthenticationStrategy;
import io.github.chcat.restclientgenerator.http.security.authentication.NoAuthenticationStrategy;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

/**
 * Created by pasa on 21.05.2015.
 */
public class ServiceBuilder {

    private String baseUrl;
    private RequestExecutor requestExecutor = new RestOperationsRequestExecutor(new RestTemplate());
    private AuthenticationStrategy authenticationStrategy = new NoAuthenticationStrategy();
    private Function<ServiceConfig,ClientFactory> factoryProducer = (c -> new CachingClientFactory(new CGLibClientFactory(new HttpControllerMethodExecutor(c))));

    public ServiceBuilder(String baseUrl) {
            this.baseUrl = baseUrl;
    }

    public ServiceBuilder useRequestExecutor(RequestExecutor requestExecutor){
        this.requestExecutor = requestExecutor;
        return this;
    }

    public Service build(){
        ServiceConfig config = new ServiceConfig(baseUrl,requestExecutor,authenticationStrategy);
        return new Service(config,factoryProducer);
    }

}
