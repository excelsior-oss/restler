package org.restler.factory;

import org.restler.ServiceConfig;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Created by pasa on 17.02.2015.
 */
public interface ControllerMethodExecutor {

    ServiceConfig getServiceConfig();

    <T> T execute(ControllerMethodDescription<T> method, Object requestBody, Map<String, ?> pathVariables, MultiValueMap<String, String> requestParams);

}
