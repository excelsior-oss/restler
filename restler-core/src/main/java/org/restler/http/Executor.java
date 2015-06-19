package org.restler.http;

import org.springframework.http.ResponseEntity;

public interface Executor {

    <T> ResponseEntity<T> execute(Request<T> request);

}
