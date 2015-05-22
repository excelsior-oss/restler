package io.github.chcat.restclientgenerator.http;

import io.github.chcat.restclientgenerator.factory.ControllerMethodExecutor;
import io.github.chcat.restclientgenerator.ServiceConfig;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    public <T> T execute(RequestMapping controllerMapping, RequestMapping methodMapping, ResponseStatus expectedStatus,
                          Object requestBody, ResponseBody methodBodyAnnotation, Class<T> returnType, Map<String, ?> pathVariables, MultiValueMap<String, String> requestParams) {

        RequestMethod declaredMethod;
        if (methodMapping.method() == null || methodMapping.method().length == 0) {
            declaredMethod = RequestMethod.GET;
        } else {
            declaredMethod = methodMapping.method()[0];
        }

        HttpMethod method = HttpMethod.valueOf(declaredMethod.toString());

        String baseUrl = null;
        String apiPath = null;
        String controllerPathSegment = getMappedUri(controllerMapping);
        String methodPathSegment = getMappedUri(methodMapping);

        URI target = UriComponentsBuilder.fromUriString(baseUrl).path(apiPath).pathSegment(controllerPathSegment,methodPathSegment).queryParams(requestParams).buildAndExpand(pathVariables).toUri();

        RequestEntity<?> requestEntity = new RequestEntity<Object>(requestBody,method,target);

        ResponseEntity<T> response = serviceConfig.getAuthenticationStrategy().executeAuthenticatedRequest(serviceConfig.getRequestExecutor(),requestEntity,returnType);

        return response.getBody();
    }

    private static String getMappedUri(RequestMapping mapping) {
        if (mapping == null) {
            return "";
        } else {
            return getFirstOrEmpty(mapping.value());
        }
    }

    private static String getFirstOrEmpty(String[] strings) {
        if (strings == null || strings.length == 0) {
            return "";
        } else {
            return strings[0];
        }
    }
}
