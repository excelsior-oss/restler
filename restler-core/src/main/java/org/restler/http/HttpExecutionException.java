package org.restler.http;

import org.restler.client.RestlerException;

import java.util.Optional;

public class HttpExecutionException extends RestlerException {

    private final String responseBody;

    public HttpExecutionException(String msg, Throwable cause, String responseBody) {
        super(msg, cause);
        this.responseBody = responseBody;
    }

    public Optional<String> getResponseBody() {
        return Optional.ofNullable(responseBody);
    }

}
