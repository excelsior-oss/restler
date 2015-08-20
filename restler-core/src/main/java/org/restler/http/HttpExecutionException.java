package org.restler.http;

public class HttpExecutionException extends RuntimeException {

    private final String responseBody;

    public HttpExecutionException(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

}
