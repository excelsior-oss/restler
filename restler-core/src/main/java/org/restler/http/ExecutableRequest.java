package org.restler.http;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;

/**
 * Describes an object that contains HTTP(S) request data and can be executed to produce a response.
 * @param <T>
 */
public class ExecutableRequest<T> {

    private HttpMethod httpMethod = HttpMethod.GET;
    private URI url;
    private Object body;
    private final Class<T> responseType;
    private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

    private final HttpRequestExecutor executor;

    public ExecutableRequest(URI url,HttpMethod httpMethod, Object body, HttpRequestExecutor executor, Class<T> responseType) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.body = body;
        this.executor = executor;
        this.responseType = responseType;
    }

    public ResponseEntity<T> execute(){
        RequestEntity<?> requestEntity = new RequestEntity<>(body, headers, httpMethod, url);

        return executor.execute(requestEntity, responseType);
    }

    public void addHeader(String name, String value){
        headers.add(name, value);
    }
}
