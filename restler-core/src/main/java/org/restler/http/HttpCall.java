package org.restler.http;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;

public class HttpCall<T> implements Call {

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

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public Call withReturnType(Type type) {
        return new HttpCall<T>(url, method, requestBody, headers, type);
    }

    public HttpCall<T> setHeader(String name, String... values) {
        ImmutableMultimap.Builder<String, String> newHeaders = new ImmutableMultimap.Builder<String, String>().putAll(headers);
        Arrays.stream(values).forEach(value -> newHeaders.put(name, value));
        return new HttpCall<>(url, method, requestBody, newHeaders.build(), returnType);
    }
}
