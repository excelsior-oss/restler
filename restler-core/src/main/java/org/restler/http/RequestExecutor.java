package org.restler.http;

import org.springframework.http.ResponseEntity;

public interface RequestExecutor {

    <T> ResponseEntity<T> execute(Request<T> request);

}
