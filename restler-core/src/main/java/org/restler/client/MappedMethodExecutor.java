package org.restler.client;

import org.restler.ServiceConfig;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * MappedMethodExecutor implementations are responsible for executing mapped controller methods
 */
public interface MappedMethodExecutor {

    ServiceConfig getServiceConfig();

    <T> T execute(MappedMethodDescription<T> method, Object requestBody, Map<String, ?> pathVariables, MultiValueMap<String, String> requestParams);

}
