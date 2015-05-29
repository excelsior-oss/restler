package org.restler.http;

import org.restler.ServiceConfig;
import org.restler.client.ServiceMethodDescription;
import org.restler.client.ServiceMethodExecutor;
import org.restler.client.ServiceMethodInvocation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class HttpServiceMethodExecutor implements ServiceMethodExecutor {

    private ServiceConfig serviceConfig;

    public HttpServiceMethodExecutor(ServiceConfig config){
        this.serviceConfig = config;
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    @Override
    public <T> T execute(ServiceMethodInvocation<T> invocation) {

        ServiceMethodDescription<T> method = invocation.getMethod();

        URI target = UriComponentsBuilder.fromHttpUrl(serviceConfig.getBaseUrl()).path(method.getUriTemplate()).queryParams(invocation.getRequestParams()).buildAndExpand(invocation.getPathVariables()).toUri();

        ExecutableRequest<T> request = new ExecutableRequest<>(target,method.getHttpMethod(), invocation.getRequestBody(),serviceConfig.getRequestExecutor(),method.getReturnType());

        ResponseEntity<T> response = serviceConfig.getAuthenticationStrategy().authenticatedAndExecute(request, serviceConfig);

        return response.getBody();
    }
}
