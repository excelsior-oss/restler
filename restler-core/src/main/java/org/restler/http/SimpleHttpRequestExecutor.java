package org.restler.http;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

public class SimpleHttpRequestExecutor implements HttpRequestExecutor {

    private final RestOperations restOperations;

    public SimpleHttpRequestExecutor(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    public <T> ResponseEntity<T> execute(ExecutableRequest<T> executableRequest) {
        RequestEntity<?> requestEntity = executableRequest.toRequestEntity();

        return restOperations.exchange(requestEntity, executableRequest.getReturnType());
    }

}
