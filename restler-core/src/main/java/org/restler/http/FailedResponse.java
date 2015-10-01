package org.restler.http;

import com.google.common.collect.ImmutableMultimap;

import java.util.Optional;

public class FailedResponse extends Response {

    private final Throwable cause;
    private final String responseBody;

    public FailedResponse(HttpStatus status, String responseBody, Throwable cause) {
        this(status, ImmutableMultimap.<String, String>of(), responseBody, cause);
    }

    public FailedResponse(HttpStatus status, ImmutableMultimap<String, String> headers, String responseBody, Throwable cause) {
        super(status, headers);
        this.cause = cause;
        this.responseBody = responseBody;
    }

    public Throwable getCause() {
        return cause;
    }

    public Optional<String> getResponseBody() {
        return Optional.ofNullable(responseBody);
    }

}
