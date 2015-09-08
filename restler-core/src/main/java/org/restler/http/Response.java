package org.restler.http;

public abstract class Response<T> {

    private final HttpStatus status;

    public Response(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
