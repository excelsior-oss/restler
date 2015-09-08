package org.restler.http;

public interface RequestExecutionAdvice {

    <T> Response<T> advice(Request<T> request, RequestExecutor requestExecutor);

}
