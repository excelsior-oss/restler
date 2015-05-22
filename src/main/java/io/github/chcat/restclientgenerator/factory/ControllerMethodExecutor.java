package io.github.chcat.restclientgenerator.factory;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

/**
 * Created by pasa on 17.02.2015.
 */
public interface ControllerMethodExecutor {

    class MethodDescription{

    }

    <T> T execute(RequestMapping controllerMapping, RequestMapping methodMapping, ResponseStatus expectedStatus, Object requestBody, ResponseBody methodBodyAnnotation, Class<T> returnType, Map<String, ?> pathVariables, MultiValueMap<String, String> requestParams);

}
