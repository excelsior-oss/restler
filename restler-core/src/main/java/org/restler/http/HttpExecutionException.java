package org.restler.http;

import org.restler.client.RestlerException;

import java.util.Optional;

public class HttpExecutionException extends RestlerException {

    private final String responseBody;
    private final HttpStatus status;

    public HttpExecutionException(String msg, Throwable cause, HttpStatus status, String responseBody) {
        super(msg, cause);
        this.status = status;
        this.responseBody = responseBody;
    }

    public Optional<String> getResponseBody() {
        return Optional.ofNullable(responseBody);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
