package io.github.chcat.restclientgenerator.http;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

/**
 * Created by pasa on 22.05.2015.
 */
public class RestOperationsRequestExecutor implements RequestExecutor {

    private final RestOperations restOperations;

    public RestOperationsRequestExecutor(RestOperations restOperations){
        this.restOperations = restOperations;
    }

    @Override
    public <T> ResponseEntity<T> execute(RequestEntity<?> request, Class<T> responseType) {
        return restOperations.exchange(request,responseType);
    }
}
