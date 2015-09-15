package org.restler.client;


public interface CallExecutionAdvice<T> {

    T advice(Call call, CallExecutor requestExecutor);

}
