package org.restler.client;

/**
 * Implementations are responsible for executing {@link Call}
 */
public interface CallExecutor {

    Object execute(Call call);

}
