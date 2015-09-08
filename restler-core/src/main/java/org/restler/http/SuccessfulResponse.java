package org.restler.http;

public class SuccessfulResponse<T> extends Response<T> {

    private T result;

    public SuccessfulResponse(HttpStatus status, T result) {
        super(status);
        this.result = result;
    }

    public T getResult() {
        return result;
    }
}
