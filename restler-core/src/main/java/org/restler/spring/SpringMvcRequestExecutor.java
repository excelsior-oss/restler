package org.restler.spring;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.restler.spring.SpringUtils.prepareForSpringMvc;
import static org.restler.spring.SpringUtils.toGuavaMultimap;

public class SpringMvcRequestExecutor implements RequestExecutor {

    private final RestTemplate restTemplate;

    public SpringMvcRequestExecutor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Response execute(Call call) {
        HttpCall springMvcCall = prepareForSpringMvc((HttpCall) call);
        RequestEntity<?> requestEntity = toRequestEntity(springMvcCall);
        ResponseEntity responseEntity = restTemplate.exchange(requestEntity, new HttpCallTypeReference<>(call));

        ImmutableMultimap<String, String> headersBuilder = toGuavaMultimap(responseEntity.getHeaders());
        HttpStatus status = new HttpStatus(responseEntity.getStatusCode().value(), responseEntity.getStatusCode().getReasonPhrase());

        return new SuccessfulResponse<>(status, headersBuilder, responseEntity.getBody());
    }

    public RequestEntity<?> toRequestEntity(HttpCall<?> call) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        call.getHeaders().entries().stream().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
        return new RequestEntity<>(call.getRequestBody(), headers, HttpMethod.valueOf(call.getHttpMethod().name()), call.getUrl());
    }

}
