package org.restler.http;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

/**
 * Implementation delegating request execution to a {@link RestOperations} instance.
 */
public class RestOperationsHttpRequestExecutor implements HttpRequestExecutor {

    private final RestOperations restOperations;

    /**
     * Creates instance delegating execution to the given instance.
     * @param restOperations an instance responsible for execution requests.
     */
    public RestOperationsHttpRequestExecutor(RestOperations restOperations){
        this.restOperations = restOperations;
    }

    @Override
    public <T> ResponseEntity<T> execute(RequestEntity<?> request, Class<T> responseType) {
        return restOperations.exchange(request,responseType);
    }
}
