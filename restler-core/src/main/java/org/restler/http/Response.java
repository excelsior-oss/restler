package org.restler.http;

import com.google.common.collect.ImmutableMultimap;

public abstract class Response {

    private final HttpStatus status;
    private final ImmutableMultimap<String, String> headers;

    public Response(HttpStatus status, ImmutableMultimap<String, String> headers) {
        this.status = status;
        this.headers = headers;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ImmutableMultimap<String, String> getHeaders() {
        return headers;
    }
}
