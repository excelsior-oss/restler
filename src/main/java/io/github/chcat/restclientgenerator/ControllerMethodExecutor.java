package io.github.chcat.restclientgenerator;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

/**
 * Created by pasa on 17.02.2015.
 */
public interface ControllerMethodExecutor {

    public Object execute(RequestMapping controllerMapping, RequestMapping methodMapping, ResponseStatus expectedStatus, Object requestBody, ResponseBody methodBodyAnnotation, Class<?> returnType, Map<String, ?> pathVariables, MultiValueMap<String, String> requestParams);

}
