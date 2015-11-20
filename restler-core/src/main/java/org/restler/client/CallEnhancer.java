package org.restler.client;


import java.util.function.BiFunction;

/**
 * CallEnhancer is additionally extension point of the library, that provides not essential features.
 */
public interface CallEnhancer extends BiFunction<Call, CallExecutor, Object> {

    /**
     * Enhances call execution.
     */
    Object apply(Call call, CallExecutor callExecutor);

}
