package org.restler.http;

import org.restler.ServiceConfig;
import org.restler.client.MappedMethodDescription;
import org.restler.client.MappedMethodExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

public class HttpMappedMethodExecutor implements MappedMethodExecutor {

    private ServiceConfig serviceConfig;

    public HttpMappedMethodExecutor(ServiceConfig config){
        this.serviceConfig = config;
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    @Override
    public <T> T execute(MappedMethodDescription<T> method, Object requestBody, Map<String, ?> pathVariables, MultiValueMap<String, String> requestParams) {

        URI target = UriComponentsBuilder.fromHttpUrl(serviceConfig.getBaseUrl()).path(method.getUriTemplate()).queryParams(requestParams).buildAndExpand(pathVariables).toUri();

        ExecutableRequest<T> request = new ExecutableRequest<>(target,method.getHttpMethod(), requestBody,serviceConfig.getRequestExecutor(),method.getReturnType());

        ResponseEntity<T> response = serviceConfig.getAuthenticationStrategy().executeAuthenticatedRequest(request);

        return response.getBody();
    }
}
