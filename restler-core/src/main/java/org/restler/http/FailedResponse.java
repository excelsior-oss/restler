package org.restler.http;

import java.util.Optional;

public class FailedResponse<T> extends Response<T> {

    private final Throwable cause;
    private final String responseBody;

    public FailedResponse(HttpStatus status, Throwable cause, String responseBody) {
        super(status);
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
