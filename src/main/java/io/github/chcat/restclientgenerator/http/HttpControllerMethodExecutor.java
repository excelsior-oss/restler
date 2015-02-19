package io.github.chcat.restclientgenerator.http;

import io.github.chcat.restclientgenerator.ControllerMethodExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

/**
 * Created by pasa on 19.02.2015.
 */
public class HttpControllerMethodExecutor implements ControllerMethodExecutor {


    @Override
    public Object execute(RequestMapping controllerMapping, RequestMapping methodMapping, RequestMethod requestType, ResponseStatus expectedStatus, Object requestBody, ResponseBody methodBodyAnnotation, Class<?> returnType, Map<String, Object> pathVariables, Map<String, Object> requestParams) {
        return null;
    }
}
