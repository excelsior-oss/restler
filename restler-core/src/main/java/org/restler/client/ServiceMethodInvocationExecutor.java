package org.restler.client;

/**
 * Implementations are responsible for executing {@link ServiceMethodInvocation}
 */
public interface ServiceMethodInvocationExecutor {

    <T> T execute(ServiceMethodInvocation<T> invocation);

}
