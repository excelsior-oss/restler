package org.restler.http;

public class HttpStatus {

    public final int code;
    public final String line;

    public HttpStatus(int code, String line) {
        this.code = code;
        this.line = line;
    }

    @Override public String toString() {
        return "HttpStatus{" +
                "code=" + code +
                ", line='" + line + '\'' +
                '}';
    }
}
