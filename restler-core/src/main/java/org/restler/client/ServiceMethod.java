package org.restler.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Describes a service method.
 *
 * @param <T> the return type of the method
 */
public class ServiceMethod<T> {
    private String uriTemplate;
    private Class<T> returnType;
    private Class returnTypeGenericType;
    private HttpMethod httpMethod;
    private HttpStatus expectedHttpResponseStatus;

    public ServiceMethod(String uriTemplate, Class<T> returnType, Class returnTypeGenericType, HttpMethod httpMethod, HttpStatus expectedHttpResponseStatus) {
        this.uriTemplate = uriTemplate;
        this.returnType = returnType;
        this.returnTypeGenericType = returnTypeGenericType;
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
    public Class<T> getReturnType() {
        return returnType;
    }

    public Class getReturnTypeGenericType() {
        return returnTypeGenericType;
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
