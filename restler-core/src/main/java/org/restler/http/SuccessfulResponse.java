package org.restler.http;

import com.google.common.collect.ImmutableMultimap;

public class SuccessfulResponse extends Response {

    private final Object result;

    public SuccessfulResponse(HttpStatus status, ImmutableMultimap<String, String> headers, Object result) {
        super(status, headers);
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
