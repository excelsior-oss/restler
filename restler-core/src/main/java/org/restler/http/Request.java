package org.restler.http;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;

/**
 * Describes an object that contains HTTP(S) request data and can be executed to produce a response.
 */
public class Request<T> {

    private HttpMethod httpMethod = HttpMethod.GET;
    private URI url;
    private Object body;
    private Class<T> returnType;
    private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

    public Request(URI url, HttpMethod httpMethod, Object body, Class<T> returnType) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.body = body;
        this.returnType = returnType;
    }

    public Request(URI url, HttpMethod httpMethod, MultiValueMap<String, String> headers, Object body, Class<T> returnType) {
        this(url, httpMethod, body, returnType);
        this.headers = headers;
    }


    public RequestEntity<?> toRequestEntity() {
        return new RequestEntity<>(body, headers, httpMethod, url);
    }

    public Class<T> getReturnType() {
        return returnType;
    }

    public Request<T> setHeader(String name, String value) {
        MultiValueMap<String, String> newHeaders = new LinkedMultiValueMap<>(headers);
        newHeaders.set(name, value);
        return new Request<>(url, httpMethod, newHeaders, body, returnType);
    }
}
