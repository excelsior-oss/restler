package org.restler.http;


import com.google.common.collect.ImmutableMultimap;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;

/**
 * Describes an object that contains HTTP(S) request data and can be executed to produce a response.
 */
public class Request<T> {

    private final HttpMethod httpMethod;
    private final URI url;
    private final Object body;
    private final Type returnType;
    private final ImmutableMultimap<String, String> headers;

    public Request(URI url, HttpMethod httpMethod, Object body, Type returnType) {
        this(url, httpMethod, new ImmutableMultimap.Builder<String, String>().build(), body, returnType);
    }

    public Request(URI url, HttpMethod httpMethod, ImmutableMultimap<String, String> headers, Object body, Type returnType) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.body = body;
        this.returnType = returnType;
        this.headers = headers;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public URI getUrl() {
        return url;
    }

    public Object getBody() {
        return body;
    }

    public ImmutableMultimap<String, String> getHeaders() {
        return headers;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Request<T> setHeader(String name, String... values) {
        ImmutableMultimap.Builder<String, String> newHeaders = new ImmutableMultimap.Builder<String, String>().putAll(headers);
        Arrays.stream(values).forEach(value -> newHeaders.put(name, value));
        return new Request<>(url, httpMethod, newHeaders.build(), body, returnType);
    }
}
