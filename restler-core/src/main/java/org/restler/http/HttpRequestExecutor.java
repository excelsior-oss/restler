package org.restler.http;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

/**
 * Implementations execute HTTP(S) requests returning HTTP(S) responses.
 */
public interface HttpRequestExecutor {

    <T> ResponseEntity<T> execute(RequestEntity<?> request, Class<T> responseType);

}
