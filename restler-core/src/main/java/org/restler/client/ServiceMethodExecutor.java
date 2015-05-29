package org.restler.client;

import org.restler.ServiceConfig;

/**
 * Implementations are responsible for executing {@link ServiceMethodInvocation}
 */
public interface ServiceMethodExecutor {

    ServiceConfig getServiceConfig();

    <T> T execute(ServiceMethodInvocation<T> invocation);

}
