package org.restler.http;


import org.springframework.http.ResponseEntity;

public interface HttpRequestExecutor {

   <T> ResponseEntity<T> execute(ExecutableRequest<T> executableRequest);

}
