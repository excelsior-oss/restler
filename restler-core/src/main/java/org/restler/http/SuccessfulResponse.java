package org.restler.http;

import com.google.common.collect.ImmutableMultimap;

public class SuccessfulResponse<T> extends Response {

    private T result;

    public SuccessfulResponse(HttpStatus status, T result) {
        this(status, ImmutableMultimap.of(), result);
    }

    public SuccessfulResponse(HttpStatus status, ImmutableMultimap<String, String> headers, T result) {
        super(status, headers);
        this.result = result;
    }

    public T getResult() {
        return result;
    }
}
