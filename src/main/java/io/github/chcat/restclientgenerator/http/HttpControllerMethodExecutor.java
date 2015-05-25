package io.github.chcat.restclientgenerator.http;

import io.github.chcat.restclientgenerator.ServiceConfig;
import io.github.chcat.restclientgenerator.factory.ControllerMethodExecutor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * Created by pasa on 19.02.2015.
 */
public class HttpControllerMethodExecutor implements ControllerMethodExecutor {

    private ServiceConfig serviceConfig;

    public HttpControllerMethodExecutor(ServiceConfig config){
        this.serviceConfig = config;
    }

    @Override
    public <T> T execute(ControllerMethodDescription<T> method, Object requestBody, Map<String, ?> pathVariables, MultiValueMap<String, String> requestParams) {

        URI target = UriComponentsBuilder.fromHttpUrl(serviceConfig.getBaseUrl()).path(method.getUriTemplate()).queryParams(requestParams).buildAndExpand(pathVariables).toUri();

        RequestEntity<?> requestEntity = new RequestEntity<>(requestBody,method.getHttpMethod(),target);

        ResponseEntity<T> response = serviceConfig.getAuthenticationStrategy().executeAuthenticatedRequest(serviceConfig.getRequestExecutor(),requestEntity,method.getReturnType());

        return response.getBody();
    }
}
