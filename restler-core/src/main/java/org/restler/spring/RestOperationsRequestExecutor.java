package org.restler.spring;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.HttpCall;
import org.restler.http.HttpStatus;
import org.restler.http.RequestExecutor;
import org.restler.http.Response;
import org.restler.http.SuccessfulResponse;
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

    public <T> Response<T> execute(HttpCall<T> call) {
        RequestEntity<?> requestEntity = toRequestEntity(call);
        ResponseEntity<T> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<T>() {
            @Override
            public Type getType() {
                return call.getReturnType();
            }
        });

        ImmutableMultimap.Builder<String, String> headersBuilder = new ImmutableMultimap.Builder<>();
        for (Map.Entry<String, List<String>> header : responseEntity.getHeaders().entrySet()) {
            headersBuilder.putAll(header.getKey(), header.getValue());
        }
        HttpStatus status = new HttpStatus(responseEntity.getStatusCode().value(), responseEntity.getStatusCode().getReasonPhrase());
        return new SuccessfulResponse<>(status, headersBuilder.build(), responseEntity.getBody());
    }

    public RequestEntity<?> toRequestEntity(HttpCall<?> call) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        call.getHeaders().entries().stream().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
        return new RequestEntity<>(call.getRequestBody(), headers, HttpMethod.valueOf(call.getHttpMethod().name()), call.getUrl());
    }

}
