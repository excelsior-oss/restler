package org.restler.http;

import org.restler.client.HttpCall;

public interface RequestExecutionAdvice {

    <T> Response<T> advice(HttpCall<T> call, RequestExecutor requestExecutor);

}
