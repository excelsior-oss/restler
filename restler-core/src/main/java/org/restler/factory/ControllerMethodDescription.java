package org.restler.factory;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public class ControllerMethodDescription<T>{
    private String uriTemplate;
    private Class<T> returnType;
    private HttpMethod httpMethod;
    private HttpStatus expectedHttpResponseStatus;

    public ControllerMethodDescription(String uriTemplate, Class<T> returnType, HttpMethod httpMethod, HttpStatus expectedHttpResponseStatus) {
        this.uriTemplate = uriTemplate;
        this.returnType = returnType;
        this.httpMethod = httpMethod;
        this.expectedHttpResponseStatus = expectedHttpResponseStatus;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public Class<T> getReturnType() {
        return returnType;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public HttpStatus getExpectedHttpResponseStatus() {
        return expectedHttpResponseStatus;
    }
}
