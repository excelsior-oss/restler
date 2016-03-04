package org.restler.client;

/**
 * Implementations are responsible for executing {@link Call}s.
 */
public interface CallExecutor {

    Object execute(Call call);

}
