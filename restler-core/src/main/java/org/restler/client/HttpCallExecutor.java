package org.restler.client;

/**
 * Implementations are responsible for executing {@link HttpCall}
 */
public interface HttpCallExecutor {

    <T> T execute(HttpCall<T> invocation);

}
