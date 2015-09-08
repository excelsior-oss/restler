package org.restler.spring;

import com.google.common.collect.ImmutableMultimap;
import org.restler.http.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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

        ImmutableMultimap.Builder<String, String> headersBuilder = new ImmutableMultimap.Builder<>();
        for (Map.Entry<String, List<String>> header : responseEntity.getHeaders().entrySet()) {
            headersBuilder.putAll(header.getKey(), header.getValue());
        }
        HttpStatus status = new HttpStatus(responseEntity.getStatusCode().value(), responseEntity.getStatusCode().getReasonPhrase());
        return new SuccessfulResponse<>(status, headersBuilder.build(), responseEntity.getBody());
    }

    public RequestEntity<?> toRequestEntity(Request<?> request) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        request.getHeaders().entries().stream().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
        return new RequestEntity<>(request.getBody(), headers, HttpMethod.valueOf(request.getHttpMethod().name()), request.getUrl());
    }

}
