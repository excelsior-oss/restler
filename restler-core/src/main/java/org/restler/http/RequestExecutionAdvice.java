package org.restler.http;


import org.springframework.http.ResponseEntity;

public interface RequestExecutionAdvice {

    <T> ResponseEntity<T> advice(Request<T> request, RequestExecutor requestExecutor);

}
