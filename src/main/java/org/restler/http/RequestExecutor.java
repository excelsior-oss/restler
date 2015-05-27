package org.restler.http;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

/**
 * Created by pasa on 22.05.2015.
 */
public interface RequestExecutor {

    <T> ResponseEntity<T> execute(RequestEntity<?> request, Class<T> responseType);

}
