package org.restler.client;

import com.google.common.collect.ImmutableMultimap;
import org.restler.http.HttpMethod;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;

public class HttpCall<T> {

    private final URI url;
    private final HttpMethod method;
    private final Object requestBody;
    private final ImmutableMultimap<String, String> headers;
    private final Type returnType;

    public HttpCall(URI url, HttpMethod method, Object requestBody,
                    ImmutableMultimap<String, String> headers, Type returnType) {
        this.url = url;
        this.method = method;
        this.requestBody = requestBody;
        this.headers = headers;
        this.returnType = returnType;
    }

    public HttpCall(URI url, HttpMethod method, Object requestBody) {
        this(url, method, requestBody, ImmutableMultimap.<String, String>of(), Object.class);
    }

    public URI getUrl() {
        return url;
    }

    public HttpMethod getHttpMethod() {
        return method;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public ImmutableMultimap<String, String> getHeaders() {
        return headers;
    }

    public Type getReturnType() {
        return returnType;
    }

    public HttpCall<T> setHeader(String name, String... values) {
        ImmutableMultimap.Builder<String, String> newHeaders = new ImmutableMultimap.Builder<String, String>().putAll(headers);
        Arrays.stream(values).forEach(value -> newHeaders.put(name, value));
        return new HttpCall<>(url, method, requestBody, newHeaders.build(), returnType);
    }
}
