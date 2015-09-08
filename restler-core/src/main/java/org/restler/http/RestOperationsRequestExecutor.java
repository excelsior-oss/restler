package org.restler.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;

public class RestOperationsRequestExecutor implements RequestExecutor {

    private final RestTemplate restTemplate;

    public RestOperationsRequestExecutor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> Response<T> execute(Request<T> request) {
        RequestEntity<?> requestEntity = toRequestEntity(request);
        ResponseEntity<T> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<T>() {
            @Override
            public Type getType() {
                return request.getReturnType();
            }
        });
        return new SuccessfulResponse<>(new HttpStatus(responseEntity.getStatusCode().value(), responseEntity.getStatusCode().getReasonPhrase()), responseEntity.getBody());
    }

    public RequestEntity<?> toRequestEntity(Request<?> request) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        request.getHeaders().entries().stream().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
        return new RequestEntity<>(request.getBody(), headers, HttpMethod.valueOf(request.getHttpMethod().name()), request.getUrl());
    }

}
