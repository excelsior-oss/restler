package org.restler.http;


import org.springframework.http.ResponseEntity;

public interface ExecutionAdvice {

    <T> ResponseEntity<T> advice(Request<T> request, Executor executor);

}
