package org.restler.client;


import java.util.function.BiFunction;

public interface CallEnhancer extends BiFunction<Call, CallExecutor, Object> {

    Object apply(Call call, CallExecutor callExecutor);

}
