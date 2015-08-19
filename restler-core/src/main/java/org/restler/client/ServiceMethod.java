package org.restler.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Type;

/**
 * Describes a service method.
 *
 * @param <T> the return type of the method
 */
public class ServiceMethod<T> {

    private final String uriTemplate;
    private final Type returnType;
    private final HttpMethod httpMethod;
    private final HttpStatus expectedHttpResponseStatus;

    public ServiceMethod(String uriTemplate, Type returnType, HttpMethod httpMethod, HttpStatus expectedHttpResponseStatus) {
        this.uriTemplate = uriTemplate;
        this.returnType = returnType;
        this.httpMethod = httpMethod;
        this.expectedHttpResponseStatus = expectedHttpResponseStatus;
    }

    /**
     * Provides a URI template the method is mapped onto.
     *
     * @return a URI template string
     */
    public String getUriTemplate() {
        return uriTemplate;
    }

    /**
     * Provides the return type of the method
     *
     * @return a type object
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Provides the {@link HttpMethod} associated with the method
     *
     * @return an HTTP method
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public HttpStatus getExpectedHttpResponseStatus() {
        return expectedHttpResponseStatus;
    }
}
