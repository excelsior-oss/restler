package org.restler;

import org.restler.factory.CGLibClientFactory;
import org.restler.factory.CachingClientFactory;
import org.restler.http.HttpControllerMethodExecutor;
import org.restler.http.RequestExecutor;
import org.restler.http.RestOperationsRequestExecutor;
import org.restler.http.security.authentication.AuthenticationStrategy;
import org.restler.http.security.authentication.NoAuthenticationStrategy;
import org.springframework.web.client.RestTemplate;

/**
 * Created by pasa on 21.05.2015.
 */
public class ServiceBuilder {

    private String baseUrl;
    private RequestExecutor requestExecutor = new RestOperationsRequestExecutor(new RestTemplate());
    private AuthenticationStrategy authenticationStrategy = new NoAuthenticationStrategy();

    public ServiceBuilder(String baseUrl) {
            this.baseUrl = baseUrl;
    }

    public ServiceBuilder useRequestExecutor(RequestExecutor requestExecutor){
        this.requestExecutor = requestExecutor;
        return this;
    }

    public Service build(){
        ServiceConfig config = new ServiceConfig(baseUrl,requestExecutor,authenticationStrategy);
        return new Service(new CachingClientFactory(new CGLibClientFactory(new HttpControllerMethodExecutor(config))));
    }

}
