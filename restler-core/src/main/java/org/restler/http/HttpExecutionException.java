package org.restler.http;

public class HttpExecutionException extends RuntimeException {

    private final String responseBody;

    public HttpExecutionException() {
        responseBody = "";
    }

    public HttpExecutionException(String responseBody) {
        this.responseBody = responseBody;
    }

    public HttpExecutionException(String message, String responseBody) {
        super(message);
        this.responseBody = responseBody;
    }

    public HttpExecutionException(String message, Throwable cause, String responseBody) {
        super(message, cause);
        this.responseBody = responseBody;
    }

    public HttpExecutionException(Throwable cause, String responseBody) {
        super(cause);
        this.responseBody = responseBody;
    }

    public HttpExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String responseBody) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.responseBody = responseBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

}
